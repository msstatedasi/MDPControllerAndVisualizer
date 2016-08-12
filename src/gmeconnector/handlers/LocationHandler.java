/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gmeconnector.handlers;

import dynamicmdpcontroller.Termination;
import gmeconnector.entities.Location;
import gmeconnector.entities.Thing;
import java.util.Vector;
import org.isis.gme.bon.JBuilderModel;
import org.isis.gme.bon.JBuilderObject;

/**
 *
 * @author stefano
 */
public class LocationHandler implements Handler {

    private static final String THING_OBJECT = "Thing";
    private static final String TERMINATION_OBJECT = "Termination";

    public Location handle(JBuilderObject o) {
        Location ret = new Location();
        JBuilderModel locationModel = (JBuilderModel) o;
        ret.setName(locationModel.getName());
        Vector locationChildren = locationModel.getChildren();

        ThingHandler th = new ThingHandler();
        for (int i = 0; i < locationChildren.size(); i++) {
            JBuilderObject child = (JBuilderObject) locationChildren.get(i);
            if (child.getMeta().getName().equals(THING_OBJECT)) {
                Thing t = th.handle(child);
                ret.addThing(t);
            } else if (child.getMeta().getName().equals(TERMINATION_OBJECT)) {
                TerminationHandler termHandler = new TerminationHandler();
                Termination t = termHandler.handle(child);
                ret.setLocalGoal(t);
            }
        }
        return ret;
    }

}
