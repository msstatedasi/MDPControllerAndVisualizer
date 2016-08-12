/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamicmdpcontroller;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import dynamicmdpcontroller.actions.DynamicMDPAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 *
 * @author stefano
 */
public class MyActionType implements ActionType {

    private final static String MY_ACTION_TYPE="MyActionType";
    private Hashtable<String, DynamicMDPAction> actions = null;
    
    
    public MyActionType() {
        actions = new Hashtable<>();
    }
    
    public void addAction(String name, DynamicMDPAction d) {
        actions.put(name, d);
    }
    
    @Override
    public String typeName() {
        return MY_ACTION_TYPE;
    }

    @Override
    public DynamicMDPAction associatedAction(String actionName) {
        return actions.get(actionName);
    }

    @Override
    public List<Action> allApplicableActions(State state) {
        List<Action> ret = new ArrayList<>();
        Enumeration<DynamicMDPAction> allTheActions = actions.elements();
        while (allTheActions.hasMoreElements()) {
            DynamicMDPAction d = allTheActions.nextElement();
            if (d.isApplicableInState(state)) {
                ret.add(d);
            }
        }
        return ret;
    }
    
    
}
