/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamicmdpcontroller.controllers;

import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FactoredModel;
import dynamicmdpcontroller.DynamicMDPState;
import dynamicmdpcontroller.MyActionType;
import dynamicmdpcontroller.Reward;
import dynamicmdpcontroller.StateModel;
import dynamicmdpcontroller.Termination;
import dynamicmdpcontroller.actions.GMEAction;
import gmeconnector.entities.Attribute;
import gmeconnector.entities.GMEActionBean;
import gmeconnector.entities.Location;
import gmeconnector.entities.Thing;
import gmeconnector.entities.VirtualEntity;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author stefano
 */
public class HomeDomainGenerator {
    
    public static final String ATT_TYPE_STRING = "String";
    public static final String ATT_TYPE_DOUBLE = "Double";
    public static final String ATT_TYPE_INTEGER = "Integer";
    public static final String ATT_TYPE_BOOLEAN = "Boolean";
    
    
    private SADomain domain = null;
    private MyActionType actionType = null;
    private StateModel sm = null;
    private Reward rf = null;
    private Termination tf = null;
    public Hashtable<String, String> attributeTypes;
    private List<GMEAction> actions = null;
    
    public HomeDomainGenerator(String termPath, double wc, double wt) throws Exception {
        attributeTypes = new Hashtable<>();
        domain = new SADomain();
        actionType = new MyActionType();
        domain.addActionType(actionType);
        sm = new StateModel();
        rf = new Reward();
        actions = new ArrayList<>();
        
        FileInputStream fis = new FileInputStream(termPath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object o = ois.readObject();
        tf = (Termination) o;
        rf.setTermination(tf);
//        tf = new Termination();
        domain.setModel(new FactoredModel(sm, rf, tf));
    }
    
    public SADomain getDomain() {
        return domain;
    }
    
    public void setDomain(SADomain domain) {
        this.domain = domain;
    }
    
    public MyActionType getActionType() {
        return actionType;
    }
    
    public void setActionType(MyActionType actionType) {
        this.actionType = actionType;
    }
    
    public StateModel getSm() {
        return sm;
    }
    
    public void setSm(StateModel sm) {
        this.sm = sm;
    }
    
    public Reward getRf() {
        return rf;
    }
    
    public void setRf(Reward rf) {
        this.rf = rf;
    }
    
    public Termination getTf() {
        return tf;
    }
    
    public void setTf(Termination tf) {
        this.tf = tf;
    }

    public List<GMEAction> getActions() {
        return actions;
    }
    
    
    
    public DynamicMDPState initialStateGenerator(List<Location> ls) {
        DynamicMDPState initialState = new DynamicMDPState();
        Iterator<Location> locations = ls.iterator();
        while (locations.hasNext()) {
            Location l = locations.next();
            String locationName = l.getName();
            Iterator<Thing> things = l.getThings().iterator();
            while (things.hasNext()) {
                Thing t = things.next();
                String thingName = t.getName();
                Iterator<VirtualEntity> ves = t.getVirtualEntities().iterator();
                while (ves.hasNext()) {
                    VirtualEntity ve = ves.next();
                    String veName = ve.getName();
                    String keyPrefix = locationName + "_" + thingName + "_" + veName + "_";
                    Iterator<Attribute> veAttributes = ve.getAttributes().iterator();
                    //Adding to the state description the service attributes
                    while (veAttributes.hasNext()) {
                        try {
                            Attribute attribute = veAttributes.next();
                            String key = keyPrefix + attribute.getAttributeName();
                            key = key.intern();
                            attributeTypes.put(key, attribute.getAttributeType());
                            if (attribute.getAttributeType().equals(ATT_TYPE_BOOLEAN)) {
                                Boolean b = Boolean.valueOf(attribute.getAttributeValue());
                                initialState.set(key, b);
                            } else if (attribute.getAttributeType().equals(ATT_TYPE_INTEGER)) {
                                Integer integer = Integer.valueOf(attribute.getAttributeValue());
                                initialState.set(key, integer);
                            } else if (attribute.getAttributeType().equals(ATT_TYPE_DOUBLE)) {
                                Double d = Double.valueOf(attribute.getAttributeValue());
                                initialState.set(key, d);
                            } else if (attribute.getAttributeType().equals(ATT_TYPE_STRING)) {
                                initialState.set(key, attribute.getAttributeValue());
                            } 
                            else {
                                //Skip attribute
                                System.out.println("Malformed Attribute. Skipping.");
                            }
//                            System.out.println("Added attribute " + key);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
        return initialState;
    }
    
    public void registerActions(List<Location> locationList) {
        Iterator<Location> locations = locationList.iterator();
        while (locations.hasNext()) {
            Location location = locations.next();
            Iterator<Thing> things = location.getThings().iterator();
            while (things.hasNext()) {
                Thing t = things.next();
                Iterator<VirtualEntity> ves = t.getVirtualEntities().iterator();
                while (ves.hasNext()) {
                    VirtualEntity ve = ves.next();
                    Iterator<GMEActionBean> actions = ve.getActions().iterator();
                    while (actions.hasNext()) {
                        GMEActionBean bean = actions.next();
                        GMEAction action = new GMEAction(bean.getPrefix() + bean.getName(), attributeTypes);
                        action.setPath(bean.getPath());
                        action.setPostCondition(bean.getPostCondition());
                        action.setPreCondition(bean.getPreCondition());
//                        action.setPrefix(bean.getPrefix());
                        action.setPrefix("");
                        action.setCost(bean.getCost());
                        action.setExecTime(bean.getExecTime());
                        action.updateHashMaps();
                        actionType.addAction(action.actionName(), action);
                        this.actions.add(action);
                    }
                }
            }
        }
        rf.cacheActionRewards(actions);
    }
    
    
}
