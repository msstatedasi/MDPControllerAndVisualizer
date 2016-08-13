/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tree;

import dynamicmdpcontroller.DecisionSupportConnection;
import dynamicmdpcontroller.DynamicMDPState;
import dynamicmdpcontroller.actions.GMEAction;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * This class contains a state and all the different connections that come from this state.
 * 
 * @author Justin Lewis
 */
public class StateNode {
    public DynamicMDPState s; //current state    
    public Hashtable<String, Connection> connections;//each connection represents one action the string is the action name
    
    
    public StateNode()
    {
        connections = new Hashtable<>();
    }


    /**
     * This function is just a setter for this instance's state
     * @param s the state to make the root state of this StateNode
     */
    public void setNodeState(DynamicMDPState s)
    {
       this.s = s.copy();
//       connections = new ArrayList();
    }
    
    
    /**
     * This function should not be called directly but rather {@link Tree.StateTree} should call this function at {@link Tree.StateTree#buildTree() }
     * when it properly sets the layout of the tree.
     * @param connect A list of the states that connect to this StateNode's state through act
     * @param act the action that connects the list connect to this StateNode's state
     */
    public void addStatesConnection(List<DynamicMDPState> connect, GMEAction act)
    {
        Connection temp = new Connection();
        temp.action = act;
        temp.states = connect;
        connections.put(act.actionName(), temp);
    }
    
    /**
     * This function just returns all the states that can result from this state
     * @return List of List of states.
     */
    public List<List<DynamicMDPState>> getAllResultingStates()
    {
        List<List<DynamicMDPState>> temp = new ArrayList<>();
        for(int i = 0; i < connections.size(); i++)
        {
            temp.add(connections.get(i).states);
        }
        return temp;
    }
    
    /**
     * This function performs a very similar job as {@link #getAllResultingStates() } execpt it returns StateNodes
     * @return A list of list of {@link Tree.StateNode}
     */
    
    public List<List<StateNode>> getAllResultingStateNodes()
    {
        List<List<StateNode>> temp = new ArrayList<>();
        for(int i = 0; i < connections.size(); i++)
        {
            temp.add(connections.get(i).nodes);
        }
        return temp;
    }
    
    
    /**
     * This function takes a {@link Tree.StateNode} and an action and adds it to the list of possible outcomes.
     * @param s One result of applying action act upon this state
     * @param act The action that caused this state to goto s.
     */
    public void addNodeConnection(StateNode s, GMEAction act)
    {
        Connection connect = this.connections.get(act.actionName());
        connect.nodes.add(s);
    }
    
    /**
     * This function checks if @param s could be a result state of this state
     * @param s the state to check if is a result of this state
     * @return the action that connects them if it exists or null if no connection exists
     */
    public GMEAction checkIfResultState(DynamicMDPState s)
    {
        for(Connection connect : this.connections.values())
        {
            List<DynamicMDPState> subList = connect.states;
            if(subList.contains(s)) return connect.action;
        }
        return null;//by the time you get here you know it does not exist
    }
    
    
    /**
     * This function returns the action name to goto s if it exists.
     * @param s the state to check if is a result and if so, return the action name that connects it to this state
     * @return action name if it exists or null if not a connecting state
     */
    public String getActionNameToGotoState(DynamicMDPState s)
    {
        return this.checkIfResultState(s).getName();
    }
    
    /**
     * This function returns the CriteriaAction to goto s if it exists.
     * @param s the state to check if is a result and if so, return the CritieriaAction that connects it to this state
     * @return the CriteriaAction that connects s to this state or null if it does not exist.
     */
    public GMEAction getCriteriaActionToGoToState(DynamicMDPState s)
    {
        return this.checkIfResultState(s);
    }
}
