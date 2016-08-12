/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gmeconnector;

import dynamicmdpcontroller.Termination;
import gmeconnector.entities.Host;
import gmeconnector.entities.Location;
import gmeconnector.handlers.LocationHandler;
import gmeconnector.handlers.TerminationHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JOptionPane;
import org.isis.gme.bon.BONComponent;
import org.isis.gme.bon.JBuilder;
import org.isis.gme.bon.JBuilderObject;

/**
 *
 * @author stefano
 */
public class MyBONComponent implements BONComponent {

    private static final String LOCATION_METANAME = "Location";
    private static final String TERMINATION_METANAME = "Termination";

    /**
     *
     */
    public MyBONComponent() {
        super();
        // TODO Auto-generated constructor stub
    }

    @Override
    public void invokeEx(JBuilder builder, JBuilderObject focus, Collection selected, int param) {
        Iterator selectedIterator = selected.iterator();
        ArrayList<Location> locations = new ArrayList<>();
        Termination t = null;
        while (selectedIterator.hasNext()) {
            JBuilderObject selection = (JBuilderObject) selectedIterator.next();
            if (selection.getMeta().getName().equals(LOCATION_METANAME)) {
                LocationHandler lh = new LocationHandler();
                Location l = lh.handle(selection);
                locations.add(l);
            } else if (selection.getMeta().getName().equals(TERMINATION_METANAME)) {
                TerminationHandler th = new TerminationHandler();
                t = th.handle(selection);
            }
        }

        String msg = new String();
        for (Location l : locations) {
            try {
                l.serialize("");
            } catch (Exception ex) {
                msg += ex.toString();
                msg += ex.getMessage();
            }
        }
        if (t != null) {
            try {
                t.serialize("");
            } catch (Exception ex) {
                msg += ex.toString();
                msg += ex.getMessage();
            }
        }
//            msg += ("hostname:" + h.getHostName() + "\n");
//            msg += ("services:\n");
//            msg += ("NICs:\n");
//            for (NIC n : h.getNics()) {
//                msg += (n.getIPAddress() + "\n");
//            }
//        }
//        String add = hostList.get(0).getServices().get(0).getActions().get(0).getName();
//        msg += ("Action Name: " + add);
        if (msg.isEmpty()) {
            msg = "Model exported successfully";
        }
        JOptionPane.showMessageDialog(null, msg);

    }

    @Override
    public void registerCustomClasses() {
    }

}
