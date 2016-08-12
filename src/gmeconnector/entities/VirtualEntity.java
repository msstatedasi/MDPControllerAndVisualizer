/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gmeconnector.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author stefano
 */
public class VirtualEntity implements Serializable {
    
    private List<Attribute> attributes = null;
    private List<GMEActionBean> actions = null;
    private String name = null;
    
    public VirtualEntity() {
        attributes = new ArrayList<>();
        actions = new ArrayList<>();
        name = new String();
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public List<GMEActionBean> getActions() {
        return actions;
    }

    public void setActions(List<GMEActionBean> actions) {
        this.actions = actions;
    }
    
    public void addAttribute(Attribute a) {
        this.attributes.add(a);
    }
    public void addAction(GMEActionBean a) {
        this.actions.add(a);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
}
