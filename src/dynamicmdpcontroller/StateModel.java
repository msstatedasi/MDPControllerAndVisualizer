/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamicmdpcontroller;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import dynamicmdpcontroller.actions.DynamicMDPAction;
import java.util.List;

/**
 *
 * @author stefano
 */
public class StateModel implements FullStateModel {

    @Override
    public List<StateTransitionProb> stateTransitions(State state, Action action) {
        DynamicMDPAction d = (DynamicMDPAction) action;
        return d.stateTransitions(state, action);
    }

    @Override
    public State sample(State state, Action action) {
        DynamicMDPAction d = (DynamicMDPAction) action;
        return d.sample(state, action);
    }

}
