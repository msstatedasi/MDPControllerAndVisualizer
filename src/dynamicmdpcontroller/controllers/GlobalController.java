/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamicmdpcontroller.controllers;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import static dynamicmdpcontroller.DynamicMDPController.nThread;
import static dynamicmdpcontroller.DynamicMDPController.stateGenThread;
import dynamicmdpcontroller.DynamicMDPState;
import dynamicmdpcontroller.Termination;
import dynamicmdpcontroller.planners.MyGreedyQPolicy;
import dynamicmdpcontroller.planners.ParallelVI;
import gmeconnector.entities.Location;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author stefano
 */
public class GlobalController {

    private HashMap<Object, Object> stateAttributes = null;
    private SADomain domain = null;
    private Episode episode = null;
    private Termination tf = null;
    private HomeDomainGenerator domainGen = null;
    private DynamicMDPState currentState = null;
    private List<Location> locations = null;
    private Termination[] localGoals = null;
    private ValueIteration planner = null;
    private Policy p = null;
    
    private double gamma;
    private double wc;
    private double wt;
    
    FilteredDomainGenerator fdg;
    String path;

    public GlobalController(String termPath, List<Location> locations,
            HashMap<Object, Object> stateAttributes, Termination[] localGoals, double gamma, double wc, double wt) throws Exception {
        this.gamma = gamma;
        this.wc = wc;
        this.wt = wt;
        this.locations = locations;
        this.stateAttributes = stateAttributes;
        this.localGoals = localGoals;
        this.currentState = init(termPath, wc, wt);
        this.path = termPath;
    }

    public Termination getTF()
    {
        return this.tf;
    }
    
        public Episode getOptimalPathFrom(DynamicMDPState s) {
        if(this.tf.isTerminal(s)) return new Episode(s);
        Episode e = PolicyUtils.rollout(p, s, fdg.getDomain().getModel());
        return e;
    }
        
    private DynamicMDPState init(String termPath, double wc, double wt) throws Exception 
    {
        System.out.println("---- Working on global home ----");
        domainGen = new HomeDomainGenerator(termPath, wc, wt);
        domainGen.initialStateGenerator(locations);
        domainGen.registerActions(locations);
        domain = domainGen.getDomain();
        currentState = new DynamicMDPState();
        currentState.putAllAttributes(stateAttributes);
        tf = domainGen.getTf();
        for (Termination t : localGoals) {
            tf.setTerminationCondition(tf.getTerminationCondition().replaceAll(t.getTerminationName(),
                    "(" + t.getTerminationCondition() + ")"));
        }
        System.out.println(domainGen.getTf().getTerminationCondition() + ": " + domainGen.getTf().isTerminal(currentState));
        return currentState;
    }

    public void planFromState(DynamicMDPState state) throws FinalStateException, Exception {
        this.stateAttributes = new HashMap<>();
        this.stateAttributes.putAll(state.getAttributes());
        this.init(path, wc, wt);
        planFromState();
    }

    public void planFromState() throws FinalStateException {
        if (domainGen.getTf().isTerminal(currentState)) {
            episode = null;
            throw new FinalStateException();
        }
                System.out.println("Termination condition: " + tf.getTerminationCondition());

        System.out.println("---- Home is not in goal state ----");
        fdg = new FilteredDomainGenerator(tf);
        DynamicMDPState filteredState = fdg.filterState(currentState, domainGen.getActions());
//        DynamicMDPState filteredState = currentState;
System.out.println(StateUtilities.stateToString(filteredState));

        SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory(false);
//        planner = new ParallelVI(fdg.getDomain(), gamma, hashingFactory, 1e-10, 100, stateGenThread, 1);
        planner = new ValueIteration(fdg.getDomain(), 0.99, hashingFactory, 1e-3, 100);
//----------------------------------------------------------------------------------
//this area is where stuff gets weird.
        planner.performReachabilityFrom(filteredState);
        p = planner.planFromState(filteredState);
        MyGreedyQPolicy qPolicy = new MyGreedyQPolicy(planner);
        episode = qPolicy.rolloutPolicy(filteredState, fdg.getDomain().getModel());
//        episode = PolicyUtils.rollout(p, filteredState, fdg.getDomain().getModel(), 20);
////------------------------------------------------------------------------------------------
//        Iterator<Action> actions = episode.actionSequence.iterator();
//        System.out.println("---- Further actions to be executed: ----");
//        while (actions.hasNext()) {
//            Action a = actions.next();
//            System.out.print(a.actionName() + ",");
//        }
    }

    public HomeDomainGenerator getDomainGen() {
        return domainGen;
    }

    public Episode getEpisode() {
        return episode;
    }

    public ValueIteration getPlanner() {
        return planner;
    }

    
    
}