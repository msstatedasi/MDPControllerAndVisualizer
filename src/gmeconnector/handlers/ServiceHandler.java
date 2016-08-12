/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gmeconnector.handlers;

import gmeconnector.entities.Attribute;
import gmeconnector.entities.GMEActionBean;
import gmeconnector.entities.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.isis.gme.bon.JBuilderModel;
import org.isis.gme.bon.JBuilderObject;

/**
 *
 * @author stefano
 */
public class ServiceHandler implements Handler {

    public static final String RESPONSEACTION_METANAME = "ResponseAction";
    public static final String SERVICEATTRIBUTE_METANAME = "ServiceAttribute";
    
    public static final String ATT_ATTRIBUTETYPE = "AttributeType";
    public static final String ATT_ATTRIBUTEVALUE = "AttributeValue";

    public Service handle(JBuilderObject o) {
        Service s = new Service();
        JBuilderModel service = (JBuilderModel) o;
        s.setServiceName(service.getName());
        Vector serviceChildren = service.getChildren();
        for (int i=0; i<serviceChildren.size();i++) {
            JBuilderObject child = (JBuilderObject) serviceChildren.get(i);
            if (child.getMeta().getName().equals(RESPONSEACTION_METANAME)) {
                GMEActionBean action = null;
                ActionHandler ah = new ActionHandler();
                action = ah.handle(child);
                s.addAction(action);
            } else if (child.getMeta().getName().equals(SERVICEATTRIBUTE_METANAME)) {
                String attributeName, attributeType, attributeValue;
                attributeName = child.getName();
                attributeType = child.getStringAttribute(ATT_ATTRIBUTETYPE);
                attributeValue = child.getStringAttribute(ATT_ATTRIBUTEVALUE);
                Attribute att = new Attribute();
                att.setAttributeName(attributeName);
                att.setAttributeType(attributeType);
                att.setAttributeValue(attributeValue);
                s.addServiceAttribute(att);
            }
        }
        return s;
    }

}
