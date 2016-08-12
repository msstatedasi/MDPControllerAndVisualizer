/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gmeconnector.handlers;

import gmeconnector.entities.Service;
import gmeconnector.entities.Host;
import gmeconnector.entities.NIC;
import java.util.Vector;
import org.isis.gme.bon.JBuilderModel;
import org.isis.gme.bon.JBuilderObject;

/**
 *
 * @author stefano
 */
public class HostHandler implements Handler {

    private static final String NIC_METANAME = "NIC";

    public Host handle(JBuilderObject o) {
        Host ret = new Host();
        JBuilderModel host = (JBuilderModel) o;
        ret.setHostName(host.getName());
        Vector hostChildren = host.getChildren();
        
        NICHandler nh = new NICHandler();
        ServiceHandler serviceHandler = new ServiceHandler();
        for (int i = 0; i < hostChildren.size(); i++) {
            JBuilderObject child = (JBuilderObject) hostChildren.get(i);
            if (child.getMeta().getName().equals(NIC_METANAME)) {
                NIC n = nh.handle(child);
                ret.addNIC(n);
            } else {
                Service service = serviceHandler.handle(child);
                ret.addService(service);
            }
        }
        return ret;
    }

}
