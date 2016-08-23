/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gmeconnector.entities;

import dynamicmdpcontroller.Termination;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author stefano
 */
public class Location implements Serializable {

    private List<Thing> things = null;
    private String name = null;
    private Termination localGoal = null;

    public Location() {
        things = new ArrayList<>();
        name = new String();
        localGoal = new Termination();
    }

    public List<Thing> getThings() {
        return things;
    }

    public void setThings(List<Thing> things) {
        this.things = things;
    }

    public void addThing(Thing t) {
        things.add(t);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void serialize(String path) throws Exception {
        FileOutputStream fos = new FileOutputStream("C:/Classes/" + name + ".location");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
        oos.close();
        fos.close();
    }

    public Termination getLocalGoal() {
        return localGoal;
    }

    public void setLocalGoal(Termination localGoal) {
        this.localGoal = localGoal;
    }
    
    
}
