/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gmeconnector.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 *
 * @author stefano
 */
public class Service implements Serializable {

   private String serviceName;
    
//    private Hashtable<String,Object> serviceAttributes = null;
    private List<GMEActionBean> actions = null;
    private List<Attribute> attributes = null;

    public Service() {
        attributes = new ArrayList<>();
        actions = new ArrayList<>();
    }
    
    public void addServiceAttribute(Attribute a) {
        this.attributes.add(a);
    }
    public List<Attribute> getAttributes() {
        return attributes;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public void addAction(GMEActionBean a) {
        actions.add(a);
    }

    public void setActions(List<GMEActionBean> actions) {
        this.actions = actions;
    }
    
    
    public List<GMEActionBean> getActions() {
        return actions;
    }
    

}
