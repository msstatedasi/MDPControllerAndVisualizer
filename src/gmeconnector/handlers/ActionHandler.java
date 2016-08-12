/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gmeconnector.handlers;

import gmeconnector.entities.GMEActionBean;
import javax.swing.JOptionPane;
import org.isis.gme.bon.JBuilderModel;
import org.isis.gme.bon.JBuilderObject;

/**
 *
 * @author stefano
 */
public class ActionHandler implements Handler {

    private static final String ATT_ACT_PRECONDITION = "ActionPrecondition";
    private static final String ATT_ACT_POSTCONDITION = "ActionPostCondition";
    private static final String ATT_ACT_PATH = "ActionPath";
    private static final String ATT_ACT_EXECTIME = "ActionExecTime";
    private static final String ATT_ACT_COST = "ActionCost";

    public GMEActionBean handle(JBuilderObject o) {
        GMEActionBean action = null;
        try {
            String name = o.getName();
            action = new GMEActionBean(name);
            String preCondition, postCondition, path;
            double[] execTime = new double[1];
            double[] cost = new double[1];
            preCondition = o.getStringAttribute(ATT_ACT_PRECONDITION);
            postCondition = o.getStringAttribute(ATT_ACT_POSTCONDITION);
            o.getAttribute(ATT_ACT_EXECTIME, execTime);
            o.getAttribute(ATT_ACT_COST, cost);
            
            path = o.getStringAttribute(ATT_ACT_PATH);
            action.setPath(path);
            action.setPostCondition(postCondition);
            action.setPreCondition(preCondition);
            action.setCost(cost[0]);
            action.setExecTime(execTime[0]);
            JBuilderModel virtualEntityObject = o.getParent();
            JBuilderModel thingObject = virtualEntityObject.getParent();
            JBuilderModel locationObject = thingObject.getParent();
            String prefix = locationObject.getName() + "_" + thingObject.getName() + "_" + virtualEntityObject.getName() + "_";
            action.setPrefix(prefix);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }

        return action;
    }
}
