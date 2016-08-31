/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamicmdpcontroller.actions;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import dynamicmdpcontroller.controllers.HomeDomainGenerator;
import dynamicmdpcontroller.DynamicMDPState;
import dynamicmdpcontroller.Expression;
import dynamicmdpcontroller.controllers.RoomDomainGenerator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.el.ELProcessor;

/**
 *
 * @author stefano
 */
public class GMEAction extends DynamicMDPAction {

    private double execTime = 0;
    private double cost = 0;

    private String prefix;
    private List<String> myAttributes = null;
    private List<String> postConditionAttributes = null;
    private List<String> preConditionAttributes = null;

    private Hashtable<String, String> attributeTypes;
    
    private List<String> actionParameters;
    private HashMap<String, Object> actionValues;

    public static final String ATT_INTERNAL_CLOCK = "InternalClock";

    public GMEAction(String actionName, Hashtable<String, String> attributeTypes) {
        super(actionName);
        myAttributes = new ArrayList<>();
        postConditionAttributes = new ArrayList<>();
        preConditionAttributes = new ArrayList<>();
        this.attributeTypes = attributeTypes;
        this.actionParameters = new ArrayList();
        this.actionValues = new HashMap<>();
        
        this.actionParameters.add("cost");
        this.actionParameters.add("exeTime");
        
        this.actionValues.put("cost", this.getCost());
    }

    @Override
    public List<StateTransitionProb> stateTransitions(State state, Action action) {
        String postConditions[] = postCondition.split(";");
        List<StateTransitionProb> transitions = new ArrayList<>(postConditions.length);
        for (int i = 0; i < postConditions.length; i++) {
            StateTransitionProb transition = parsePostCondition(postConditions[i], state);
            transitions.add(transition);
        }
        return transitions;
    }

    @Override
    public State sample(State state, Action action) {
        List<StateTransitionProb> transitions = stateTransitions(state, action);
        double rand = Math.random();
        double probSum = 0;
        Iterator<StateTransitionProb> transIte = transitions.iterator();
        State nextState = null;
        while (transIte.hasNext() && probSum < rand) {
            StateTransitionProb trans = transIte.next();
            probSum += trans.p;
            nextState = trans.s;
        }
        return nextState;
    }

    private StateTransitionProb parsePostCondition(String s, State src) {
        StateTransitionProb ret = new StateTransitionProb();
        DynamicMDPState dest = (DynamicMDPState) src.copy();
        String lhs = s.split(":")[0];
        String rhs = s.split(":")[1];
        double prob = Double.valueOf(lhs.split("=")[1]);
        ret.p = prob;
        String[] postConditionElems = rhs.split(",");
        for (int i = 0; i < postConditionElems.length; i++) {
            String postConditionElem = postConditionElems[i];
            String attributeKey = postConditionElem.split("=")[0];
            String attributeValue = postConditionElem.split("=")[1];
//            String attributeType = DynamicMDPDomainGenerator.attributeTypes.get(prefix + attributeKey);
            String attributeType = attributeTypes.get(prefix + attributeKey);

            if (attributeType == null) {
                System.out.println();
            }

            //Post-condition attributes caching
            if (postConditionAttributes.isEmpty()) {
                postConditionAttributes = new ArrayList<>();
                for (String attr : myAttributes) {
                    if (attributeValue.contains(attr)) {
                        postConditionAttributes.add(attr);
                    }
                }
            }
            //Handling numeric and boolean expression
            if (!attributeType.equals(HomeDomainGenerator.ATT_TYPE_STRING)) {

                Expression expr = new Expression(attributeValue);

                for (String attr : postConditionAttributes) {
                    Object curAttrValue = src.get(attr);
                    expr = expr.with(attr, curAttrValue.toString());
                }
                BigDecimal bd = expr.eval();
                if (attributeType.equals(HomeDomainGenerator.ATT_TYPE_BOOLEAN)) {
                    int intValue = bd.intValue();
                    boolean value;
                    if (intValue == 0) {
                        value = false;
                    } else {
                        value = true;
                    }
                    dest.set(prefix + attributeKey, value);
                } else if (attributeType.equals(HomeDomainGenerator.ATT_TYPE_INTEGER)) {
                    dest.set(prefix + attributeKey, bd.intValue());
                } else if (attributeType.equals(HomeDomainGenerator.ATT_TYPE_DOUBLE)) {
                    dest.set(prefix + attributeKey, bd.doubleValue());
                }

            } else if (attributeType.equals(HomeDomainGenerator.ATT_TYPE_STRING)) {
                dest.set(prefix + attributeKey, attributeValue);
            } else {
                System.out.println("Malformed postcondition or attribute!");
            }
        }
        ret.s = dest;
        return ret;
    }

    @Override
    public boolean isApplicableInState(State s) 
    {
        if (myAttributes == null || myAttributes.isEmpty()) {
            List<Object> attributeKeys = s.variableKeys();
            for (Object o : attributeKeys) {
                String attributeKey = (String) o;
                if (attributeKey.startsWith(prefix)) {
                    myAttributes.add(attributeKey);
                }
            }
        }

        //Pre-condition attributes caching
        if (preConditionAttributes.isEmpty()) {
            for (String attr : myAttributes) {
                if (super.preCondition.contains(attr)) {
                    preConditionAttributes.add(attr);
                }
            }
        }

        ELProcessor elprocessor = new ELProcessor();
        Iterator<String> attIterator = preConditionAttributes.iterator();
        while (attIterator.hasNext()) {
            String attribute = attIterator.next();
            String attributeSuffix = attribute;

            elprocessor.setValue(attributeSuffix, s.get(attribute));
        }
        Object eval = null;
        try {
            eval = elprocessor.eval(super.preCondition);
        } catch (Exception e) 
        {
            System.out.println(this.name);
            e.printStackTrace();
        }
        boolean ret = Boolean.valueOf(eval.toString());
        return ret;
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

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public double getExecTime() {
        return execTime;
    }

    public void setExecTime(double execTime) {
        this.execTime = execTime;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public List<String> getPostConditionAttributes() {
        return postConditionAttributes;
    }

    public List<String> getPreConditionAttributes() {
        return preConditionAttributes;
    }

    public List<String> actionAttributes() {
        List<String> actionAttributes = new ArrayList<>();
        for (String s : myAttributes) {
            if (preCondition.contains(s)) {
                actionAttributes.add(s);
            }
        }

        String postConditions[] = postCondition.split(";");
        for (String postCondition : postConditions) {
            String rhs = postCondition.split(":")[1];
            String[] postConditionElems = rhs.split(",");
            for (String postConditionElem:postConditionElems) {
                String attributeName = postConditionElem.split("=")[0];
                if (myAttributes.contains(attributeName)) {
                    actionAttributes.add(attributeName);
                }
                String attributeValue = postConditionElem.split("=")[1];
                for (String s:myAttributes) {
                    if (attributeValue.contains(s))
                        actionAttributes.add(s);
                }
            }
        }
        return actionAttributes;
    }

    public List<String> getActionParameterNames() {
        List<String> names = new ArrayList();
        names.add("cost");
        names.add("exeTime");
        return names;
    }
    public void updateHashMaps() 
    {
        this.actionValues.put("cost", this.getCost());
        this.actionValues.put("exeTime", this.getExecTime());
    }

    public Object getActionParameterValues(String valueName) {
        return actionValues.get(valueName);
    }

}
