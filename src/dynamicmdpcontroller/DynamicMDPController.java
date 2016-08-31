/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamicmdpcontroller;

import dynamicmdpcontroller.controllers.RoomDomainGenerator;
import dynamicmdpcontroller.controllers.LocalController;
import burlap.behavior.singleagent.Episode;
import burlap.mdp.singleagent.SADomain;
import dynamicmdpcontroller.controllers.FinalStateException;
import dynamicmdpcontroller.controllers.GlobalController;
import gmeconnector.entities.Location;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author stefano
 */
public class DynamicMDPController {

    /**
     * @param args the command line arguments
     */
    public static int stateGenThread;
    public static int nThread;
    private static String locationDir;

    private LocalController[] localControllers = null;
    private GlobalController globalController = null;

    private static DynamicMDPController controller = null;



    public DynamicMDPController() 
    {
        
    }

    public static DynamicMDPController getInstance() {
        if (controller == null) {
            controller = new DynamicMDPController();
        }
        return controller;
    }

    public static void main(String[] args) throws Exception {

        locationDir = args[0];
        stateGenThread = Integer.valueOf(args[1]);
        nThread = Integer.valueOf(args[2]);

        controller = DynamicMDPController.getInstance();
        controller.run(1, 0 , .99);
    }

    private void run(double wc, double wt, double gamma) throws Exception {
        //Reading all the room informations
        List<Location> locations = new ArrayList<>();
        File dir = new File(locationDir);
        File[] locationFiles = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".location");
            }
        });

        for (File locationFile : locationFiles) {
            FileInputStream fis = new FileInputStream(locationFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Location l = (Location) ois.readObject();
            locations.add(l);
        }

        //Running local MDPs
        localControllers = new LocalController[locations.size()];
        HashMap<Object, Object> mergedAttributes = new HashMap<>();
        Termination[] localGoals = new Termination[locations.size()];
        for (int i = 0; i < locations.size(); i++) {
            localControllers[i] = new LocalController(locations.get(i), wc, wt, gamma);
            try 
            {
                localControllers[i].planFromState();
            } 
            catch (FinalStateException ex) 
            {
                System.out.println("No need to optimize: subsystem already in final state");
            }
            mergedAttributes.putAll(localControllers[i].getStateAttributes());
            localGoals[i] = localControllers[i].getTf();
        }

        //Generating full MDP
        globalController = new GlobalController(locationDir + "/termination.term", locations, mergedAttributes, localGoals, wc, wt, gamma);
//        try {
//            globalController.planFromState();
//        } catch (FinalStateException ex) {
//            System.out.println("No need to optimize: system already in final state");
//        }

    }

    public LocalController[] getLocalControllers() {
        return localControllers;
    }

    public GlobalController getGlobalController() {
        return globalController;
    }

    public void setupAndRunController(double cost, double time, double gamma) throws Exception {
        locationDir = "C:/Classes/";
        stateGenThread = 4;
        nThread = 4;
        
        controller = DynamicMDPController.getInstance();
        controller.run(cost, time, gamma);
    }
    
    

   
}
