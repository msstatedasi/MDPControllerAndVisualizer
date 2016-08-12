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
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import dynamicmdpcontroller.DynamicMDPController;
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
public class LocalController {

    Location l = null;
    private HashMap<Object, Object> stateAttributes = null;
    private SADomain domain = null;
    private Episode episode = null;
    private Termination tf = null;
    private RoomDomainGenerator domainGen = null;
    private DynamicMDPState initialState = null;
    private ValueIteration planner = null;
    Policy p = null;
    private double gamma = 0;
    private double wc;
    private double wt;

    public LocalController(Location l, double wc, double wt, double gamma) throws Exception {
        this.l = l;
        this.wc = wc;
        this.wt = wt;
        this.gamma = gamma;
        initialState = init();
        stateAttributes = new HashMap<>();

    }

    private DynamicMDPState init() throws Exception {
        System.out.println("---- Working on room: " + l.getName() + " ----");
        domainGen = new RoomDomainGenerator(l, wc, wt);
        domainGen.registerActions();
        domain = domainGen.getDomain();
        initialState = domainGen.initialStateGenerator();

        tf = domainGen.getTf();
        return initialState;
    }

    public void planFromState(DynamicMDPState initialState) throws FinalStateException {
        this.initialState = initialState;
        planFromState();
    }

    public void planFromState() throws FinalStateException {
        if (domain.getModel().terminal(initialState)) 
        {
            episode = null;
            stateAttributes.putAll(initialState.getAttributes());
            throw new FinalStateException();
        }
        SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory(false);
//            ValueIteration planner = new ValueIteration(domain, 0.99, hashingFactory, 1e-3, 100);
 
        planner = new ParallelVI(domain, this.gamma, hashingFactory,
                1e-10, 100, DynamicMDPController.stateGenThread, DynamicMDPController.nThread);
        DynamicMDPState finalState = initialState;
        System.out.println("---- " + l.getName() + " is not in goal state ----");
//                planner.performReachabilityFrom(initialState);
        p = planner.planFromState(initialState);
        episode = PolicyUtils.rollout(p, initialState, domain.getModel());
        Iterator<Action> actions = episode.actionSequence.iterator();
        
        System.out.println("---- Actions to be executed in the " + l.getName() + ": ----");

        while (actions.hasNext()) {
            Action a = actions.next();
            System.out.print(a.actionName() + ",");
        }
        System.out.println();

        List<Double> rewards = episode.rewardSequence;
        System.out.println("Policy length: " + rewards.size());
        for (Double d : rewards) {
            System.out.print(d + ", ");
        }
        System.out.println();
        System.out.println("Discounted return: " + episode.discountedReturn(0.99));
        List<State> stateSequence = episode.stateSequence;
        finalState = (DynamicMDPState) stateSequence.get(stateSequence.size() - 1);
        stateAttributes.putAll(finalState.getAttributes());
    }

    public HashMap<Object, Object> getStateAttributes() {
        return stateAttributes;
    }

    public Termination getTf() {
        return tf;
    }

    public RoomDomainGenerator getDomainGen() {
        return domainGen;
    }

    public Episode getEpisode() {
        return episode;
    }

    public ValueIteration getPlanner() {
        return planner;
    }

    public Episode getOptimalPathFrom(DynamicMDPState s) {
        Episode e = PolicyUtils.rollout(p, s, domain.getModel());
        return e;
    }

    public DynamicMDPState getInitState() {
        return initialState;
    }
    
    

}
