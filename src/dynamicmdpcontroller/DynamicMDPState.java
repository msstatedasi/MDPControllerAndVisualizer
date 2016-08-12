/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamicmdpcontroller;

import burlap.mdp.core.state.MutableState;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author stefano
 */
public class DynamicMDPState implements MutableState {

    private Hashtable<Object, Object> attributes;
    private boolean forbidden = false;

    public Hashtable<Object, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Hashtable<Object, Object> attributes) {
        this.attributes = attributes;
    }

    public DynamicMDPState() {
        attributes = new Hashtable<>();
        forbidden=false;
    }
    
    public void putAllAttributes(Map<Object,Object> map) {
        attributes.putAll(map);
    }

    public DynamicMDPState(Hashtable<Object, Object> attrs) {
        this.attributes = attrs;
        this.forbidden=false;
    }
    public DynamicMDPState(Hashtable<Object, Object> attrs, boolean forbidden) {
        this.attributes = attrs;
        this.forbidden = forbidden;
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        if (attributes.containsKey(variableKey)) {
            attributes.replace(variableKey, value);
        } else {
            attributes.put(variableKey, value);
        }
        return this;
    }

    @Override
    public List<Object> variableKeys() {
        ArrayList keyList = new ArrayList(attributes.keySet());
        return keyList;
    }

    @Override
    public Object get(Object key) {
        return attributes.get(key);
    }

    public boolean isForbidden() {
        return forbidden;
    }

    public void setForbidden(boolean forbidden) {
        this.forbidden = forbidden;
    }
    
    

    @Override
    public DynamicMDPState copy() {
//        Hashtable newAttr = new Hashtable(attributes);
        Hashtable newAttr = new Hashtable(attributes.size());
        Iterator<Object> keys = attributes.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            key = key.intern();
            newAttr.put(key, attributes.get(key));
        }
        return new DynamicMDPState(newAttr,forbidden);
    }
    
        @Override
    public boolean equals(Object o) {

        boolean ret = false;
        if (o instanceof DynamicMDPState) {
            DynamicMDPState s = (DynamicMDPState) o;
            ret = s.getAttributes().equals(this.attributes);
        }
        return ret;
    }
    @Override

    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.attributes);
        return hash;
    }

}
