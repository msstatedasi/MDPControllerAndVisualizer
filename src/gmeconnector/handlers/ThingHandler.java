/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gmeconnector.handlers;

import gmeconnector.entities.Service;
import gmeconnector.entities.Host;
import gmeconnector.entities.NIC;
import gmeconnector.entities.Thing;
import gmeconnector.entities.VirtualEntity;
import java.util.Vector;
import org.isis.gme.bon.JBuilderModel;
import org.isis.gme.bon.JBuilderObject;

/**
 *
 * @author stefano
 */
public class ThingHandler implements Handler {


    public Thing handle(JBuilderObject o) {
        Thing ret = new Thing();
        JBuilderModel thingModel = (JBuilderModel) o;
        ret.setName(thingModel.getName());
        Vector thingChildren = thingModel.getChildren();
        VirtualEntityHandler veHandler = new VirtualEntityHandler();
        for (int i = 0; i < thingChildren.size(); i++) {
            JBuilderObject child = (JBuilderObject) thingChildren.get(i);
            VirtualEntity ve = veHandler.handle(child);
            ret.addVirtualEntity(ve);
        }
        return ret;
    }

}
