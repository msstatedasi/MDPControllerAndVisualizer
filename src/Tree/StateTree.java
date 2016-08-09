/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tree;


import dynamicmdpcontroller.DynamicMDPState;
import dynamicmdpcontroller.actions.GMEAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

/**
 * This class acts as the major container for the state space and in a format that is easy to 
 * gather the info the visualizer needs in an easy fashion.
 * @author Justin Lewis
 * 
 */
public class StateTree {
    public DynamicMDPState initialState; //used to apply actions to get to target state
    public List<GMEAction> actionsTaken; //actions from initial state to target state
    public List<DynamicMDPState> statesTaken; //states from initial to target
    public List<StateNode> stateNodesTaken;//like statesTaken but is the StateNodes(easy way to see different paths that could have been taken)
    public StateNode initialNode;         //like initial state but the StateNode version
    public HashMap<DynamicMDPState, StateNode> nodes;           //all nodes in the state space
    public List<GMEAction> allPossibleActions;//every defined action in the MDP
    
    /**
     * Constructor for StateTree.
     * @param init The initial state.
     * @param n List of {@link Tree.StateNode} that contains the entire state space
     * @param allPos List of CriteriaAction that contains every defined action
     */
    public StateTree(DynamicMDPState init, HashMap<DynamicMDPState, StateNode> n, List<GMEAction> allPos)
    {
        allPossibleActions = allPos;
        initialState = init;
        nodes = n;
        stateNodesTaken = new ArrayList();
    }
    
    /**
     * Get the entire state space in the form of a list of {@link Tree.StateNode}
     * @return List of {@link Tree.StateNode}
     */
    public HashMap<DynamicMDPState, StateNode> getNodes()
    {
        return nodes;
    }
    
    public StateNode getNodeForState(DynamicMDPState s)
    {
        return nodes.get(s);
    }
    

    /**
     * Set the actions that were taken from initial state to the final state
     * @param t List of CriteriaAction
     */
    public void setTakenActions(List<GMEAction> t)
    {
        actionsTaken = t;
    }
    
    /**
     * This function set the states that were taken to go from the initial state to the final state
     * @param s List of States taken to go from initial to final state.
     */
    public void setStatesTaken(List<DynamicMDPState> s)
    {
        statesTaken = s;
    }
    
 
    /**
     * This function actually connects the connections of connections of {@link Tree.StateNode} together.
     * <p>
     * As described in the JavaDoc in {@link Tree.StateNode#addNodeConnection(Tree.StateNode, burlapcontroller.actions.CriteriaAction) }
     * simply just places the connection in that node and that connection cannot be seen in any other {@link Tree.StateNode}.
     * This function interweaves those connections together to make this tree fully connected.
     */
    public void buildTree()
    {   
        initialNode = nodes.get(initialState);
        
        

        
        //the below loop is used to fill the stateNodesTaken ArrayList
        //stateNodesTaken is used by the visualizer to more easily access certain elements
        //regarding their transistions
        for(int i = 0; i < statesTaken.size(); i++)//for each state in states taken
        {
            stateNodesTaken.add(nodes.get(statesTaken.get(i)));
        }

        
        //the below loop adds the connections between every node
        for(StateNode starting : nodes.values())
        {
            
            
            for(StateNode comp : nodes.values()) //these nested loops basically act as something like insertion sort
            {
//                StateNode starting = nodes.get(u);
//                StateNode comp = nodes.get(i);
                
                GMEAction act = starting.checkIfResultState(comp.s);//are these nodes connected?
                if(act != null)//if they are
                {
                    starting.addNodeConnection(comp, act);//add their connection
                }
            }
        } 
        
        
    }//end of buildTree
}//end of class
