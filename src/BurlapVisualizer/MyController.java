 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BurlapVisualizer;

import Tree.StateNode;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.planning.Planner;
import dynamicmdpcontroller.DecisionSupportConnection;
import dynamicmdpcontroller.DynamicMDPController;
import dynamicmdpcontroller.DynamicMDPState;
import dynamicmdpcontroller.actions.GMEAction;
import dynamicmdpcontroller.controllers.FinalStateException;
import dynamicmdpcontroller.controllers.LocalController;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

/**
 * This class acts as an extension to {@link burlapcontroller.Controller} to provide methods to more easily
 * grab the data needed for the visualizer.
 * @author Justin Lewis
 */
public class MyController extends DynamicMDPController {
    Planner planner;
    List<DynamicMDPState> statespace;
    Policy p;   
    HashMap<DynamicMDPState, StateNode> nodes;
    double gamma;
    DecisionSupportConnection dsc;
    
    
    /**
     * This is the constructor for MyController which inherits {@link burlapcontroller.Controller} to add functionality needed to easily
     * grab the info needed for the visualizer(such as connections between state.
     * @param scan
     * @param vsftpd
     * @param smbd
     * @param phpcgi
     * @param ircd
     * @param distccd
     * @param rmi
     * @param ip
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public MyController(double cost, double time, double gamma) throws FileNotFoundException, IOException, Exception {
//        super(scan, vsftpd, smbd, phpcgi, ircd, distccd, rmi, ip, time, cost, impact);
        
        super.setupAndRunController(cost, time, gamma);
        nodes = new HashMap<>();
        this.gamma = gamma;
        dsc = new DecisionSupportConnection();
    }
    
    public int getNumOfLocalControllers()
    {
        return dsc.getNumOfLocalControllers();
    }

    
    /**
     * This basically actions as a getter function to grab the state Sequence.
     * @return List of states taken to get to the target state 
     */
    public List<DynamicMDPState> getStateSequenceToTarget(int index) throws FinalStateException
    {
        return dsc.getLocalOptimalPath(index, dsc.getInitalState(index));
    }
    
    public double getGamma()
    {
        return this.gamma;
    }
    public String getStateString(DynamicMDPState s)
    {
        return dsc.stateString(s);
    }
    public Object getValueForAttribute(DynamicMDPState s, String str)
    {
        return dsc.getValueForAttribute(s, str);
    }
    
    public List<String> getActionParamters(GMEAction a)
    {
        return a.actionAttributes();
    }
    
    
    /**
     * This functions gets the entire state space and applies every action that can be applied to any given state.
     * <p>
     * An important note about what this function does not do: It only provides links from one state to another state.
     * What this means is the each {@link Tree.StateNode} only contains the root state and then children states from applying 1 action.
     * To get the full connections between states(applying more than 1 action to a state) {@link Tree.StateTree#buildTree() } must be called AFTER
     * this method has been called.
     * @return List of {@link Tree.StateNode StateNode} that contains all the resulting states given any arbitrary state.
     * @throws IOException 
     */
    public HashMap<DynamicMDPState, StateNode> getEntireStateSpaceAndConnections(int index) throws IOException
    {
        List<DynamicMDPState> states = dsc.getAllStates(index); //get all possible states

        List<GMEAction> allListActions = dsc.getAllLocalDefinedActions(0);
        Hashtable<String, GMEAction> map = new Hashtable<>();
        for(int a = 0; a < allListActions.size(); a++)
        {
            map.put(allListActions.get(a).actionName(), allListActions.get(a));
        }
        for(int counter = 0; counter < states.size(); counter++) //iteratte through all states
        {
            DynamicMDPState s = states.get(counter);  //current state we are working with
            StateNode current = new StateNode();//eventually put the state into here
            current.setNodeState(s);           //set the node root to state(now to fill in the actions
            
            List<GMEAction> allActions = dsc.getAllLocalDefinedActions(0);
//            try//see below comment about why this try catch block exists
            {
                //the line below fails if it returns empty list....weird seems like a bug which is also the purpose for the try catch

                for(int i = 0; i < allActions.size(); i++) //go through each action and apply it
                {           
                    List<DynamicMDPState> result = dsc.getResultingStates(s, allActions.get(i));
                    if(result.isEmpty()) continue;
                    current.addStatesConnection(result, allActions.get(i));//add that list of resulting states into
                }
            }
//            catch(IndexOutOfBoundsException e)
            {
                //this is only here because when no actions
                //can be performed meaning it is a final state
                //the getActionDisttributionForState(s) is the offending function.
                //I swear that function has got a bug.
            }

            nodes.put(current.s, current); //add node to list of nodes
            
        }
        return nodes; //finally return our list of nodes with possible states
    }
    
    //this function gets only the OPTIMAL episode in each local Controller
    public Episode getEpisode(int index) throws FinalStateException
    {
        List<DynamicMDPState> states = dsc.getLocalOptimalPath(index, dsc.getInitalState(index));
        List<GMEAction> actions = dsc.getLocalOptimalPathActions(index, dsc.getInitalState(index));
        Episode e = new Episode();
        
        for(DynamicMDPState s: states)
        {
            e.stateSequence.add(s);
        }
        for(GMEAction a:actions)
        {
            e.actionSequence.add(a);
        }
        return e;
    }
    public void printState(DynamicMDPState s)
    {
        dsc.printState(s);
    }
    
    
    public List<String> allDefinedActions(int index)
    {
        List<GMEAction> actions = dsc.getAllLocalDefinedActions(index);
        List<String> actionStrings = new ArrayList<>();
        for(int i = 0; i < actions.size(); i++)
        {
            actionStrings.add(actions.get(i).actionName());
        }
        return actionStrings;
    }
    
    /**
     * This method just takes a state and applied to passed in action and records the possible outcomes from that action.
     * @param s The state that is having an action applied to it.
     * @param act This action that is being applied to @param s
     * @return A list of the possible result states
     */
    private List<DynamicMDPState> getResultingStates(DynamicMDPState s, GMEAction act)
    {
        return dsc.getResultingStates(s, act);
    }
    
    /**
     * This function returns a list of all the attribute names any state has.
     * <p>
     * This function can be used in conjunction to {@link #getAllStateValuesFor(burlap.oomdp.core.states.State)}
     * to get the value of the corresponding name.  Since they both return arrays you must use parralel lists to make sure the name
     * and value pair go together.  For example index 1 refers to the same attribute in the lists returned by both functions.
     * @return List of all the names of the attributes any state has in the state space
     */
    public List<String> getAllStateAttributes(int index)
    {
        return dsc.getAllStateAttributes(dsc.getInitalState(index));
    }

    

   

   
   
        /**
         * This function computes the total cost, impact, and time the policy took and returns it.
         * <p>
         * The array represents all three values in the following format:
         * [0] = cost
         * [1] = impact
         * [2] = time
         * @param actions list of actions from initial state to target state
         * @return the array of values in the specified format
         */
        public double[] computeTotalReward(List<GMEAction> actions)
        {
            double[] Reward = new double[2];
            Reward[0] = 0.0;
            Reward[1] = 0.0;
            for(int i = 0; i < actions.size(); i++)
            {
                //grap the cost, impact, and time to do each action and add to the previous sum.
                Reward[0] += actions.get(i).getCost();
                Reward[1] += actions.get(i).getExecTime();
            }
            return Reward;
        }
        
        public double getReward(List<GMEAction> acts, List<DynamicMDPState> states) 
        {
            double sum = 0;
            sum = dsc.getLocalPathReward(0, acts, states);
            return sum;
        }
        
        public double getReward(GMEAction act, DynamicMDPState state)
        {
            List<GMEAction> singleAction = new ArrayList();
            singleAction.add(act);
            List<DynamicMDPState> singleState = new ArrayList();
            singleState.add(state);
            return this.getReward(singleAction, singleState);
        }

    public Hashtable<String, GMEAction> getActionMap(int index) 
    {
        Hashtable<String, GMEAction> table  = new Hashtable<>();
        List<GMEAction> allActions = dsc.getAllLocalDefinedActions(index);
        for(GMEAction a:allActions)
        {
            table.put(a.actionName(), a);
        }
        return table;
    }

    public double getV(int index, DynamicMDPState get) throws FinalStateException 
    {
        return dsc.getLocalStateValue(index, get);
    }
    
    public boolean isTerminal(int index, DynamicMDPState s)
    {
        return dsc.isTerminalState(index, s);
    }

    public Episode getOptimalPathFrom(int index, DynamicMDPState s) throws FinalStateException 
    {
        return dsc.getEpisodeFromState(index, s);
    }
}
