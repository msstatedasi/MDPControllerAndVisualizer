/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gmeconnector.entities;

import com.sun.corba.se.impl.orbutil.ObjectWriter;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author stefano
 */
public class Host implements Serializable {
    
    private String hostName;
    private List<NIC> nics;
    private List<Service> services;
    
    public Host() {
        services = new ArrayList<>();
        nics = new ArrayList<>();
    }
    
    public void addNIC(NIC n) {
        nics.add(n);
    }
    
    public void addService(Service as) {
        services.add(as);
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public List<NIC> getNics() {
        return nics;
    }

    public List<Service> getServices() {
        return services;
    }
    
    public void serialize(String path) throws Exception {
        FileOutputStream fos = new FileOutputStream("C:/Classes/"+hostName+".host");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
        oos.close();
        fos.close();
    }
    
    
}
