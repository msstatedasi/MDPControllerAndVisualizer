/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gmeconnector.handlers;

import gmeconnector.entities.Attribute;
import gmeconnector.entities.GMEActionBean;
import gmeconnector.entities.Service;
import gmeconnector.entities.VirtualEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.isis.gme.bon.JBuilderModel;
import org.isis.gme.bon.JBuilderObject;

/**
 *
 * @author stefano
 */
public class VirtualEntityHandler implements Handler {

    public static final String RESPONSEACTION_METANAME = "VirtualEntityActuator";
    public static final String SERVICEATTRIBUTE_METANAME = "VirtualEntitySensor";
    
    public static final String ATT_ATTRIBUTETYPE = "AttributeType";
    public static final String ATT_ATTRIBUTEVALUE = "AttributeValue";

    public VirtualEntity handle(JBuilderObject o) {
        VirtualEntity ve = new VirtualEntity();
        JBuilderModel veModel = (JBuilderModel) o;
        ve.setName(veModel.getName());
        Vector veChildren = veModel.getChildren();
        for (int i=0; i<veChildren.size();i++) {
            JBuilderObject child = (JBuilderObject) veChildren.get(i);
            if (child.getMeta().getName().equals(RESPONSEACTION_METANAME)) {
                GMEActionBean action = null;
                ActionHandler ah = new ActionHandler();
                action = ah.handle(child);
                ve.addAction(action);
            } else if (child.getMeta().getName().equals(SERVICEATTRIBUTE_METANAME)) {
                String attributeName, attributeType, attributeValue;
                attributeName = child.getName();
                attributeType = child.getStringAttribute(ATT_ATTRIBUTETYPE);
                attributeValue = child.getStringAttribute(ATT_ATTRIBUTEVALUE);
                Attribute att = new Attribute();
                att.setAttributeName(attributeName);
                att.setAttributeType(attributeType);
                att.setAttributeValue(attributeValue);
                ve.addAttribute(att);
            }
        }
        return ve;
    }

}
