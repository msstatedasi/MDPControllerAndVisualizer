/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tree;


import dynamicmdpcontroller.DynamicMDPState;
import dynamicmdpcontroller.actions.GMEAction;
import java.util.ArrayList;
import java.util.List;

/**
 * This class just acts as a container for what defines a connection between a state and an action.
 * <p>
 * Each instance of this class simply contains 1 action and a list of possible states.  Because all data types are public
 * there is no need for getter or setter methods.
 * @author Justin Lewis
 */
public class Connection {
    
    public GMEAction action;
    public List<DynamicMDPState> states;
    public List<StateNode> nodes;

    /**
     * Constructor just defines list variables
     */
    public Connection() 
    {
        states = new ArrayList();
        nodes = new ArrayList();
    }
}
