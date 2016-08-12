/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamicmdpcontroller.actions;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.SimpleAction;
import burlap.mdp.core.state.State;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author stefano
 */

public abstract class DynamicMDPAction extends SimpleAction {
    protected String preCondition;
    protected String postCondition;
    protected String path;
    
    public DynamicMDPAction(String actionName) {
        this.setName(actionName);
    }

    public String getPreCondition() {
        return preCondition;
    }

    public void setPreCondition(String preCondition) {
        this.preCondition = preCondition;
    }

    public String getPostCondition() {
        return postCondition;
    }

    public void setPostCondition(String postCondition) {
        this.postCondition = postCondition;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    
    @Override
    public String getName() {
        return super.actionName();
    }
    
    
    public boolean isApplicableInState(State s) {
        return true;
    }
    
    public abstract List<StateTransitionProb> stateTransitions(State state, Action action);
    
    public abstract State sample(State state, Action action);
        
    
}
