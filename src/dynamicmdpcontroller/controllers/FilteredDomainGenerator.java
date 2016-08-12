/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamicmdpcontroller.controllers;

import burlap.mdp.core.action.ActionType;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FactoredModel;
import dynamicmdpcontroller.DynamicMDPState;
import dynamicmdpcontroller.MyActionType;
import dynamicmdpcontroller.Reward;
import dynamicmdpcontroller.StateModel;
import dynamicmdpcontroller.Termination;
import dynamicmdpcontroller.actions.GMEAction;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author stefano
 */
public class FilteredDomainGenerator {

    private List<GMEAction> actions = null;
    
    private SADomain domain = null;
    private MyActionType actionType = null;
    private StateModel sm = null;
    private Reward rf = null;
    private Termination tf = null;
    public Hashtable<String, String> attributeTypes;

    public FilteredDomainGenerator(Termination t) {
        domain = new SADomain();
        actionType = new MyActionType();
        domain.addActionType(actionType);
        sm = new StateModel();
        rf = new Reward();
        actions = new ArrayList<>();
        this.tf = t;
        rf.setTermination(tf);
        domain.setModel(new FactoredModel(sm, rf, tf));
    }

    public DynamicMDPState filterState(DynamicMDPState currentState, List<GMEAction> actions) {
        //The following is only needed to generate the action's internal data structures
        for (GMEAction a : actions) {
            a.isApplicableInState(currentState);
            a.stateTransitions(currentState, null);
        }
        List<String> requiredAttributes = new ArrayList<>();
        List<GMEAction> requiredActions = new ArrayList<>();
        List<String> terminationAttributes = tf.getTerminationAttributes();

        LinkedList<String> attributesToProcess = new LinkedList<>();
        attributesToProcess.addAll(terminationAttributes);
        while (!attributesToProcess.isEmpty()) {
            String attribute = attributesToProcess.poll();
            if (!requiredAttributes.contains(attribute)) {
                requiredAttributes.add(attribute);
                List<GMEAction> filteredActions = getActionsByAttributeName(attribute, actions);
                for (GMEAction a : filteredActions) {
                    attributesToProcess.addAll(a.actionAttributes());
                    requiredActions.add(a);
                    actionType.addAction(a.getName(), a);
                }
            }
        }
        this.actions = requiredActions;
        rf.cacheActionRewards(actions);

        DynamicMDPState filteredState = new DynamicMDPState();
        HashMap<Object, Object> filteredAttributes = new HashMap<>();
        for (String s : requiredAttributes) {
            Object value = currentState.get(s);
            filteredAttributes.put(s, value);
        }
        filteredState.putAllAttributes(filteredAttributes);
        return filteredState;
    }
    

    private List<GMEAction> getActionsByAttributeName(String attributeName, List<GMEAction> actions) {
        List<GMEAction> ret = new ArrayList<>();
        for (GMEAction a : actions) {
//            if (a.getName().equals("Livingroom_AugmentedAirConditioning_VirtualAirConditioning_TurnOn")) {
//                System.out.println();
//            }
            List<String> actionAttributes = a.actionAttributes();

            if (actionAttributes.contains(attributeName)) {
                ret.add(a);
            }
        }
        return ret;
    }

    public SADomain getDomain() {
        return domain;
    }
    
    
}
