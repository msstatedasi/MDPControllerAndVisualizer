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
    
    private double gamma;
    private double wc;
    private double wt;

    public GlobalController(String termPath, List<Location> locations,
            HashMap<Object, Object> stateAttributes, Termination[] localGoals, double gamma, double wc, double wt) throws Exception {
        this.gamma = gamma;
        this.wc = wc;
        this.wt = wt;
        this.locations = locations;
        this.stateAttributes = stateAttributes;
        this.localGoals = localGoals;
        this.currentState = init(termPath, wc, wt);
    }

    private DynamicMDPState init(String termPath, double wc, double wt) throws Exception {
        System.out.println("---- Working on global home ----");
        domainGen = new HomeDomainGenerator(termPath, wc, wt);
        domainGen.initialStateGenerator(locations);
        domainGen.registerActions(locations);
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

    public void planFromState(DynamicMDPState state) throws FinalStateException {
        this.currentState = state;
        planFromState();
    }

    public void planFromState() throws FinalStateException {
        if (domainGen.getTf().isTerminal(currentState)) {
            episode = null;
            throw new FinalStateException();
        }
        System.out.println("---- Home is not in goal state ----");
        FilteredDomainGenerator fdg = new FilteredDomainGenerator(tf);
        DynamicMDPState filteredState = fdg.filterState(currentState, domainGen.getActions());
        System.out.println(StateUtilities.stateToString(filteredState));

        SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory(false);
        planner = new ParallelVI(fdg.getDomain(), gamma, hashingFactory, 1e-10, 100, stateGenThread, nThread);
//            ValueIteration planner = new ValueIteration(fdg.getDomain(), 0.99, hashingFactory, 1e-3, 100);
//            planner.performReachabilityFrom(filteredState);
        Policy p = planner.planFromState(filteredState);
        episode = PolicyUtils.rollout(p, filteredState, fdg.getDomain().getModel());
        Iterator<Action> actions = episode.actionSequence.iterator();
        System.out.println("---- Further actions to be executed: ----");
        while (actions.hasNext()) {
            Action a = actions.next();
            System.out.print(a.actionName() + ",");
        }
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