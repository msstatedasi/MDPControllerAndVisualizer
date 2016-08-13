/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamicmdpcontroller;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import dynamicmdpcontroller.actions.GMEAction;
import dynamicmdpcontroller.controllers.FinalStateException;
import dynamicmdpcontroller.controllers.GlobalController;
import dynamicmdpcontroller.controllers.LocalController;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author stefano
 */
public class DecisionSupportConnection implements DecisionSupportInterface 
{
    private LocalController localControllers[] = null;
    private GlobalController globalController = null;

    private Episode[] localEpisode = null;
    private Episode globalEpisode = null;

    public DecisionSupportConnection() {
        localControllers = DynamicMDPController.getInstance().getLocalControllers();
        globalController = DynamicMDPController.getInstance().getGlobalController();
        localEpisode = new Episode[localControllers.length];
    }

    private void planFromLocalState(DynamicMDPState s, int index) throws FinalStateException {
        localControllers[index].planFromState(s);
    }

    private void planFromGlobalState(DynamicMDPState s) throws FinalStateException {
        globalController.planFromState(s);
    }

    @Override
    public List<GMEAction> getAllLocalDefinedActions(int index) {
        return localControllers[index].getDomainGen().getActions();
    }

    @Override
    public List<GMEAction> getAllGlobalDefinedActions() {
        return globalController.getDomainGen().getActions();
    }

    @Override
    public void printState(DynamicMDPState s) {
        System.out.println(StateUtilities.stateToString(s));
    }

    @Override
    public List<String> getAllStateAttributes(DynamicMDPState s) {
        List<Object> keys = s.variableKeys();
        List<String> ret = new ArrayList<>(keys.size());
        for (Object o : keys) 
        {
            ret.add((String) o);
        }
        return ret;
    }

    @Override
    public List<GMEAction> getLocalOptimalPathActions(int index, DynamicMDPState d) throws FinalStateException {
        if (localEpisode[index] == null) {
//            planFromLocalState(d, index);
            localEpisode[index] = localControllers[index].getEpisode();
        }
        List<Action> actions = localEpisode[index].actionSequence;
        List<GMEAction> ret = new ArrayList<>(actions.size());
        for (Action a : actions) {
            GMEAction ga = (GMEAction) a;
            ret.add(ga);
        }
        return ret;
    }

    @Override
    public List<GMEAction> getGlobalOptimalPathActions(DynamicMDPState s) throws FinalStateException {
        if (globalEpisode == null) {
            planFromGlobalState(s);
            globalEpisode = globalController.getEpisode();
        }
        List<Action> actions = globalEpisode.actionSequence;
        List<GMEAction> ret = new ArrayList<>(actions.size());
        for (Action a : actions) {
            ret.add((GMEAction) a);
        }
        return ret;
    }

    @Override
    public List<DynamicMDPState> getGlobalOptimalPath(DynamicMDPState s) throws FinalStateException {
        if (globalEpisode == null) {
            planFromGlobalState(s);
            globalEpisode = globalController.getEpisode();
        }
        List<State> states = globalEpisode.stateSequence;
        List<DynamicMDPState> ret = new ArrayList<>(states.size());
        for (State st : states) {
            ret.add((DynamicMDPState) st);
        }
        return ret;
    }

    @Override
    public List<DynamicMDPState> getLocalOptimalPath(int index, DynamicMDPState s) throws FinalStateException 
    {
        if (localEpisode[index] == null) 
        {
//            localControllers[index].planFromState(s);
            localEpisode[index] = localControllers[index].getEpisode();
        }
        DynamicMDPState first = (DynamicMDPState) localEpisode[index].stateSequence.get(0);
        if(first.equals((s)))
        {
            List<State> states = localEpisode[index].stateSequence;
            List<DynamicMDPState> ret = new ArrayList<>(states.size());
            for (State st : states) 
            {
                ret.add((DynamicMDPState) st);
            }
            return ret;
        }
        else
        {
            Episode e = new Episode(s);
            List<DynamicMDPState> states = new ArrayList();
            for(State state: e.stateSequence)
            {
                states.add((DynamicMDPState) state);
            }
            return states;
        }
    }

    @Override
    public double getGlobalPathReward(DynamicMDPState s) throws FinalStateException {
        if (globalEpisode == null) {
            planFromGlobalState(s);
            globalEpisode = globalController.getEpisode();
        }
        List<Double> rewards = globalEpisode.rewardSequence;
        double ret = 0;
        for (Double d : rewards) {
            ret += d;
        }
        return ret;
    }

    @Override
    public double getLocalPathReward(int index, DynamicMDPState s) throws FinalStateException {
        if (localEpisode[index] == null) 
        {
            localControllers[index].planFromState(s);
            localEpisode[index] = localControllers[index].getEpisode();
        }
        List<Double> rewards = localEpisode[index].rewardSequence;
        double ret = 0;
        for (Double d:rewards) 
        {
            ret+=d;
        }
        return ret;
    }
    @Override
    public double getLocalPathReward(int index, List<GMEAction> actions, List<DynamicMDPState> states) 
    {
        double reward = 0;
        for(int i = 0; i < actions.size(); i++)
        {
            reward += this.localControllers[0].getDomainGen().getRf().reward(states.get(i), actions.get(i), states.get(i));
        }
        return reward;
        
    }

    @Override
    public double getLocalStateValue(int index, DynamicMDPState s) throws FinalStateException {
        if (localEpisode[index] == null) 
        {
            localControllers[index].planFromState(s);
            localEpisode[index] = localControllers[index].getEpisode();
        }
        if(localControllers[index].getPlanner().value(s) != 0) return localControllers[index].getPlanner().value(s); //ask stefano here
//ValueIteration test = localControllers[index].getPlanner();
        return localControllers[index].getPlanner().performBellmanUpdateOn(s);
//        return localControllers[index].getPlanner().value(s);
    }

    @Override
    public double getGlobalStateValue(DynamicMDPState s) throws FinalStateException {
        if (globalEpisode == null) 
        {
            planFromGlobalState(s);
            globalEpisode = globalController.getEpisode();
        }
        return globalController.getPlanner().value(s);
    }

    @Override
    public DynamicMDPState getInitalState(int index) 
    {
        return this.localControllers[index].getInitState();
    }

    @Override
    public List<DynamicMDPState> getAllStates(int index) {
        List<DynamicMDPState> allStates = new ArrayList();
        List<DynamicMDPState> toCompute = new ArrayList();
        toCompute.add(this.getInitalState(index));
        allStates.add(this.getInitalState(index));
        
        for(int i = 0; i < toCompute.size(); i++)
        {
            System.out.println(toCompute.size());
            for(int j = 0; j < this.getAllLocalDefinedActions(index).size(); j++)
            {
                GMEAction a = (GMEAction) this.getAllLocalDefinedActions(index).get(j);
                DynamicMDPState s = toCompute.get(i);
                List<DynamicMDPState> resultStates = this.getResultingStates(s.copy(), a);
                
                for (DynamicMDPState resultState : resultStates) 
                {
                    if(!toCompute.contains(resultState))
                    {
                        toCompute.add(resultState);
                        allStates.add(resultState);
                    }
                }
            }
        }
        return allStates;
    }

    @Override
    public List<DynamicMDPState> getResultingStates(DynamicMDPState s, GMEAction a) 
    {     
        if(!a.isApplicableInState(s)) return new ArrayList<>(); //required statement ask stefano
        List<StateTransitionProb> tp = a.stateTransitions(s.copy(), a.copy());
        if(tp == null) return new ArrayList();
        List<DynamicMDPState> resultingStates = new ArrayList();
        
        for(int i = 0; i < tp.size(); i++)
        {
            resultingStates.add((DynamicMDPState) tp.get(i).s);
            if(resultingStates.get(i).equals(s)) System.out.println("action " + a.actionName() + " does same state");
        }
//        System.out.println("return with " + resultingStates.size() + "    " + resultingStates.get(0).equals(s));
        return resultingStates;
    }

    @Override
    public boolean isTerminalState(int index, DynamicMDPState s) 
    {
        return localControllers[index].getTf().isTerminal(s);
    }

    @Override
    public String stateString(DynamicMDPState s) 
    {
        return StateUtilities.stateToString(s);
    }

    @Override
    public Object getValueForAttribute(DynamicMDPState s, String str) 
    {
//        String fullStr = "Network_Server_ServiceGroup_" + str;
        return s.getAttributes().get(str);
    }

    @Override
    public Episode getEpisodeFromState(int index, DynamicMDPState s) 
    {
        Episode e = localControllers[index].getOptimalPathFrom(s);
        return e;
    }

    @Override
    public int getNumOfLocalControllers() {
        return this.localControllers.length;
    }

    @Override
    public String getNameOfController(int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose 
    }
}
