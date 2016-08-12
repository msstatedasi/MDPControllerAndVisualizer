/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamicmdpcontroller.planners;

import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.SampleModel;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author stefano
 */
public class ParallelVI extends ValueIteration {

    private double delta[];
    private Map<HashableState, Double>[] valueFunctionArray;
    private int nThread = 0;
    int stateGenThread = 0;

    private LinkedList<HashableState> openList = new LinkedList<>();
    private Set<HashableState> openedSet = new HashSet<>();

    public synchronized void addToList(HashableState s) {
        openList.offer(s);
    }

    public synchronized void addToSet(HashableState s) {
        openedSet.add(s);
    }

    public synchronized boolean setContains(HashableState s) {
        return openedSet.contains(s);
    }

    public synchronized HashableState listPoll() {
        return openList.poll();
    }

    public synchronized boolean valueFunctionContainsKey(HashableState s) {
        return valueFunction.containsKey(s);
    }

    public SampleModel getModel() {
        return this.model;
    }

    public boolean getStopReachabilityFromTerminalStates() {
        return this.stopReachabilityFromTerminalStates;
    }

    public synchronized void valueFunctionPut(HashableState sh) {
        this.valueFunction.put(sh, this.valueInitializer.value(sh.s()));
    }
    
    public List<Action> getApplicableActions(State s) {
        return this.applicableActions(s);
    }

    @Override
    public synchronized double value(HashableState sh) {
        return super.value(sh);
    }

    public void setDelta(double delta, int index) {
        this.delta[index] = delta;
    }

    public ParallelVI(SADomain domain, double gamma, HashableStateFactory hashingFactory, double maxDelta, int maxIterations, int stateGenThread, int nThread) {
        //Where are reward function and terminal function?
        super(domain, gamma, hashingFactory, maxDelta, maxIterations);
        valueFunctionArray = new HashMap[nThread];
        for (int i = 0; i < nThread; i++) {
            valueFunctionArray[i] = new HashMap<>();
        }
        this.nThread = nThread;
        this.stateGenThread = stateGenThread;
        delta = new double[nThread];
    }

    /**
     * Runs VI until the specified termination conditions are met. In general,
     * this method should only be called indirectly through the
     * {@link #planFromState(State)} method. The
     * {@link #performReachabilityFrom(State)} must have been performed at least
     * once in the past or a runtime exception will be thrown. The
     * {@link #planFromState(State)} method will automatically call the
     * {@link #performReachabilityFrom(State)} method first and then this if it
     * hasn't been run.
     */
    @Override
    public void runVI() {

        if (!this.foundReachableStates) {
            throw new RuntimeException("Cannot run VI until the reachable states have been found. Use the planFromState or performReachabilityFrom method at least once before calling runVI.");
        }
        Set<HashableState> states = super.valueFunction.keySet();
        List<Set<HashableState>> subsets = this.subset(states);

        ParallelVIWorker workers[] = new ParallelVIWorker[nThread];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new ParallelVIWorker();
            workers[i].setParallelVI(this);
            workers[i].setStates(subsets.get(i));
            workers[i].setIndex(i);
        }
        for (int j = 0; j < nThread; j++) {
            delta[j] = Double.POSITIVE_INFINITY;
        }

        for (int i = 0; i < this.maxIterations; i++) {

            System.out.println("Starting " + nThread + " threads");
            Thread t[] = new Thread[nThread];
            for (int j = 0; j < t.length; j++) {
                t[j] = new Thread(workers[j]);
                t[j].start();
            }
            for (int j = 0; j < t.length; j++) {
                try {
                    t[j].join();
//                    System.out.println("Joined: " + j);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ParallelVI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
//            valueFunction.clear();
            for (Map<HashableState, Double> m : valueFunctionArray) {
                valueFunction.putAll(m);
            }

            boolean completed = true;
            for (int j = 0; j < nThread; j++) {
                completed = completed & delta[j] < this.maxDelta;
            }
            if (completed) {
                break;
            }
        }
        this.hasRunVI = true;
        System.out.println("Optimization Completed");
    }

    public double getMaxDelta() {
        return maxDelta;
    }

    private List<Set<HashableState>> subset(Set<HashableState> set) {
        List<Set<HashableState>> theSets = new ArrayList<Set<HashableState>>(nThread);
        for (int i = 0; i < nThread; i++) {
            theSets.add(new HashSet<HashableState>());
        }

        int index = 0;
        for (HashableState object : set) {
            theSets.get(index++ % nThread).add(object);
        }

        return theSets;
    }

    private synchronized List<Set<HashableState>> stateSubset(LinkedList<HashableState> list) {
        List<Set<HashableState>> theSets = new ArrayList<Set<HashableState>>(stateGenThread);
        for (int i = 0; i < stateGenThread; i++) {
            theSets.add(new HashSet<HashableState>());
        }

        int index = 0;
        while (!list.isEmpty()) {
            theSets.get(index++ % stateGenThread).add(list.poll());
        }
        return theSets;
    }

    /**
     * Performs a Bellman value function update on the provided (hashed) state.
     * Results are stored in the value function map as well as returned. If this
     * object is set to used cached transition dynamics and the transition
     * dynamics for this state are not cached, then they will be created and
     * cached.
     *
     * @param sh the hashed state on which to perform the Bellman update.
     * @return the new value of the state.
     */
    public double performBellmanUpdateOn(HashableState sh, int index) {

        if (model.terminal(sh.s())) {
            //terminal states always have a state value of 0
            valueFunctionArray[index].put(sh, 0.);
            return 0.;
        }

        List<Action> gas = this.applicableActions(sh.s());
        double[] qs = new double[gas.size()];
        int i = 0;
        for (Action ga : gas) {
            double q = this.computeQ(sh.s(), ga);
            qs[i] = q;
            i++;
        }
        double nv = operator.apply(qs);

        valueFunctionArray[index].put(sh, nv);

        return nv;
    }

    /**
     * This method will find all reachable states that will be used by the
     * {@link #runVI()} method and will cache all the transition dynamics. This
     * method will not do anything if all reachable states from the input state
     * have been discovered from previous calls to this method.
     *
     * @param si the source state from which all reachable states will be found
     * @return true if a reachability analysis had never been performed from
     * this state; false otherwise.
     */
//    @Override
//    public boolean performReachabilityFrom(State si) {
//
//        HashableState sih = this.stateHash(si);
//        //if this is not a new state and we are not required to perform a new reachability analysis, then this method does not need to do anything.
//        if (valueFunction.containsKey(sih) && this.foundReachableStates) {
//            return false; //no need for additional reachability testing
//        }
//
//        DPrint.cl(this.debugCode, "Starting reachability analysis");
//
//        //add to the open list
//        openList = new LinkedList<>();
//        openedSet = new HashSet<>();
//        openList.offer(sih);
//
//        StateGeneratorSetWorker[] workers = new StateGeneratorSetWorker[stateGenThread];
//        Thread[] threads = new Thread[stateGenThread];
//        int prevStatesNumber = 0;
//        int curStateNumber = 0;
//        while (!openList.isEmpty()) {
//            Date d1 = new Date();
//            List<Set<HashableState>> setList = this.stateSubset(openList);
//            for (int i = 0; i < stateGenThread; i++) {
//                workers[i] = new StateGeneratorSetWorker(this);
//                workers[i].setState(setList.get(i));
//                threads[i] = new Thread(workers[i]);
//                threads[i].start();
//            }
//            for (int i = 0; i < stateGenThread; i++) {
//                try {
//                    threads[i].join();
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(ParallelVI.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//            prevStatesNumber = curStateNumber;
//            curStateNumber = valueFunction.size();
//            Date d2 = new Date();
//            double stateGenerationRate = ((curStateNumber - prevStatesNumber) / ((d2.getTime() - d1.getTime()) / (double) 1000));
//            System.out.println(valueFunction.size() + " generated states, " + openList.size() + " still to process at " + stateGenerationRate + " states/sec");
//        }
//
//        DPrint.cl(this.debugCode, "Finished reachability analysis; # states: " + valueFunction.size());
//
//        this.foundReachableStates = true;
//        this.hasRunVI = false;
//
//        return true;
//
//    }

}
