/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gmeconnector.handlers;

import gmeconnector.entities.NIC;
import org.isis.gme.bon.JBuilderObject;

/**
 *
 * @author stefano
 */
public class NICHandler implements Handler {

    private static final String ATT_IPADDR = "IPAddr";

    public NIC handle(JBuilderObject o) {
        NIC ret = new NIC();
        String ipAddr = o.getStringAttribute(ATT_IPADDR);
        ret.setIPAddress(ipAddr);
        return ret;
    }

}
