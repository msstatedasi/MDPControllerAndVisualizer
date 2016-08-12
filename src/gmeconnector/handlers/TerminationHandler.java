/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gmeconnector.handlers;

import dynamicmdpcontroller.Termination;
import org.isis.gme.bon.JBuilderModel;
import org.isis.gme.bon.JBuilderObject;

/**
 *
 * @author stefano
 */
public class TerminationHandler implements Handler {

    private static final String ATT_EXPRESSION = "Expression";

    @Override
    public Termination handle(JBuilderObject o) {
        Termination t = new Termination();
        String expr = o.getStringAttribute(ATT_EXPRESSION);
        t.setTerminationCondition(expr);
        JBuilderModel parent = o.getParent();
        String terminationName = "";
        if (parent != null) {
            terminationName+=(parent.getName() + "_");
        }
        terminationName+=o.getName();
        t.setTerminationName(terminationName);
        return t;
    }

}
