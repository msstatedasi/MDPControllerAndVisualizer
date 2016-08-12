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
public class Thing implements Serializable {
    
    private List<VirtualEntity> virtualEntities = null;
    private String name = null;
    
    public Thing() {
        virtualEntities = new ArrayList<>();
        name = new String();
    }

    public List<VirtualEntity> getVirtualEntities() {
        return virtualEntities;
    }

    public void setVirtualEntities(List<VirtualEntity> virtualEntities) {
        this.virtualEntities = virtualEntities;
    }
    public void addVirtualEntity(VirtualEntity ve) {
        virtualEntities.add(ve);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
    
    
}
