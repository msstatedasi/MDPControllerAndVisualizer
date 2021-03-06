/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BurlapVisualizer;

import Tree.StateNode;
import Tree.StateTree;
import dynamicmdpcontroller.DecisionSupportConnection;
import dynamicmdpcontroller.DynamicMDPState;
import dynamicmdpcontroller.actions.GMEAction;
import dynamicmdpcontroller.controllers.FinalStateException;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import visual.DataDisplay;
import visual.Visualizer;

/**
 * This class manages how {@link BurlapVisualizer.MyController} and
 * {@link visual.Visualizer} interact with each other.
 *
 * @author Justin Lewis
 */
public class Manager {
    MyController c;
    HashMap<DynamicMDPState, StateNode> nodes; //list of all nodes in state space
    List<DynamicMDPState> takenStates;//list of all the states taken
    List<GMEAction> takenActions;//list of all the actions taken
    Visualizer MDPvisual;//an instance of the visualizer
    double degredation;
    DataDisplay dd;
    List<String> controllerNames;
    int numOfPushedButtons = 0;
    List<DynamicMDPState> finalStates;
    List<String> globalAttribs;
    DecisionSupportConnection dsc;
    boolean visualizerRunning = false;

    /**
     * This function is only called by {@link BurlapVisualizer.TestProject} when the
     * user specifies a different initial state.
     * <p>
     * All this function does is call {@link visual.Visualizer#closeWindows() }
     * to close all windows with the old initial state.
     */
    public void close() throws FinalStateException, IOException, ParseException, Exception {
        if(MDPvisual != null) MDPvisual.closeWindows();
    }

    /**
     * When this function is called, everything about the initial state is known
     * and it is time to set everything up for the visualization.
     * <p>
     * This function is responsible for getting all the connections between
     * states by calling the appropriate functions.  After it gets these connections
     * it sends that data to the visualizer and gives it complete control
     *
     * @param scan
     * @param vsftpd
     * @param smbd
     * @param phpcgi
     * @param ircd
     * @param distccd
     * @param rmi
     * @param cost 
     * @param impact
     * @param time
     * @throws Exception
     */
    public void run(double cost, double time, double gamma, double degredation) throws Exception 
    {
        
        this.finalStates = new ArrayList();
        this.globalAttribs = new ArrayList();
        this.degredation = degredation;
        c = new MyController(cost, time, gamma);//this sets up and solves the MDP solution
        this.dsc = c.getDSC();
        
        int numOfControllers = c.getNumOfLocalControllers();
        controllerNames = c.getAllControllerNames(numOfControllers);
        JPanel panel = new JPanel(new GridLayout(0, 3));
        
        for(String text : controllerNames)
        {
            JButton controllerButton = new JButton();
            controllerButton.setText(text);
            controllerButton.addActionListener(new buttonAction(controllerButton));
            panel.add(controllerButton);
        }
        JFrame frame = new JFrame();
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
        
        List<String> allStateAttribs = new ArrayList();
        
        for(int i = 0; i < c.getNumOfLocalControllers(); i++)
        {
            for(int j = 0; j < c.getAllStateAttributes(i).size(); j++)
            {
                allStateAttribs.add(c.getAllStateAttributes(i).get(j));
            }
        }
        dd = new DataDisplay(allStateAttribs, c);
    }
    
    public class buttonAction implements ActionListener
    {
        JButton button;
        
        
        public buttonAction(JButton button)
        {
            this.button = button;
        }
        
        public void PathFinished(DynamicMDPState s) throws FinalStateException, IOException, ParseException, Exception
        {
            visualizerRunning = false;
            finalStates.add(s);
            if(numOfPushedButtons == controllerNames.size())
            {
                DynamicMDPState finalState = new DynamicMDPState();
                for(DynamicMDPState dmdps : finalStates)
                {
                    finalState.putAllAttributes(dmdps.getAttributes());
                }
                handleGlobalPath(finalState);
                
            }
        }
        
        
        public void handleGlobalPath(DynamicMDPState s) throws IOException, ParseException, Exception
        {
            try
            {
//                DecisionSupportConnection dsc = new DecisionSupportConnection();
                    takenActions = dsc.getGlobalOptimalPathActions(s);
                    takenStates = dsc.getGlobalOptimalPath(s);//the states taken from intial state to target state

                
                
                List<GMEAction> allPosActions = dsc.getAllGlobalDefinedActions();//this is a list of every action that the MDP has defined
                
                
                //now we have all our data we need for the visualizer.
                //To make things easier I created the StateTree class which can wrap all
                //this data into one nice data structure
                StateTree tree = new StateTree(dsc.getGlobalinitState(), nodes, allPosActions); //here I had s but replaced with dsc.getGlobalInitState
                tree.setTakenActions(takenActions);
                tree.setStatesTaken(takenStates);
//                tree.buildTree();//this sets the connections between connections that c.getIntireStatSpaceAndConnections() did not do.

                MDPvisual = new Visualizer(tree, globalAttribs, allPosActions, c, degredation, -1, dd, this);//the visualizer takes over from here
            } catch (FinalStateException ex) {
//                return;
//                Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            if(visualizerRunning) return;
            visualizerRunning = true;
            if(MDPvisual != null) MDPvisual.closeChart();
            try 
            {
                numOfPushedButtons++;
                this.button.setEnabled(false);
                int length = e.getActionCommand().split(" ").length;
                int index = controllerNames.indexOf(e.getActionCommand());
                
                
                
                
                
                
                List<String> allAttribs = c.getAllStateAttributes(index);
                for(String str : allAttribs)
                {
                    globalAttribs.add(str);
                }
                
//                nodes = c.getEntireStateSpaceAndConnections(index);//nodes is a list that contains trees with a height of at most 2.
                //for more info about what c.getEntireStateSpaceAndConnections() does look
                //at the JavaDoc
                
                
                
                takenActions = dsc.getLocalOptimalPathActions(index, dsc.getInitalState(index));//the actions taken from intial state to target state
                takenStates = dsc.getLocalOptimalPath(index, dsc.getInitalState(index));//the states taken from intial state to target state
                
                if(takenStates == null)
                {
                    PathFinished(dsc.getInitalState(index));
                    return;
                }
                
                List<GMEAction> allPosActions = dsc.getAllLocalDefinedActions(index);//this is a list of every action that the MDP has defined
                
                
                //now we have all our data we need for the visualizer.
                //To make things easier I created the StateTree class which can wrap all
                //this data into one nice data structure
                StateTree tree = new StateTree(dsc.getInitalState(index), nodes, allPosActions);
                tree.setTakenActions(takenActions);
                tree.setStatesTaken(takenStates);
//                tree.buildTree();//this sets the connections between connections that c.getIntireStatSpaceAndConnections() did not do.
                
                MDPvisual = new Visualizer(tree, allAttribs, allPosActions, c, degredation, index, dd, this);//the visualizer takes over from here
                
                
                
            } catch (FinalStateException | IOException | ParseException ex) {
                Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
}
