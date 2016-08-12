package visual;


import BurlapVisualizer.MyController;
import Tree.Connection;
import Tree.StateNode;
import dynamicmdpcontroller.DynamicMDPState;
import dynamicmdpcontroller.actions.GMEAction;
import dynamicmdpcontroller.controllers.FinalStateException;
import java.awt.BasicStroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.tuple.TableEdgeItem;
import prefuse.visual.tuple.TableNodeItem;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * This class is our handle to the user's mouse.  Based on what the user clicks there are specific things desired which are handled here.
 * @author Justin Lewis
 */
public class FinalControlListener extends ControlAdapter implements Control {
    

    public String ItemClicked;
    GMEAction clickedAction = null;
    
    DataDisplay dataDisplay;
    
    List<NodeItem> highlightedNodes;
    TableEdgeItem prevEdge;
    
    int prevnodeInfoNum;
    NodeItem prevNode;
    Chart chart;
    StateValueContainer chosenSVC;
    ActionValueContainer chosenAVC;
    JPopupMenu menu;
    NodeItem currentState;
    
    Visualizer thisVis;

    /**
     * All constructor does is initialize variables
     */
    public FinalControlListener(MyController c) //rewardfunction was here
    {
        highlightedNodes = new ArrayList<>();
        chosenAVC = new ActionValueContainer(c);
        chosenSVC = new StateValueContainer(chosenAVC);
        menu = new JPopupMenu();
    }
    
    /**
     * This function sets the {@link visual.DataDisplay} needed for the control listener.
     * <p>
     * The reason for having a dataDisplay in the control listener is so that it can be called
     * with update info whenever the user clicks a node or edge.
     * @param dataDisplay this is the handle to the window with all the info about the visualizer
     */
    public void setDataDisplay(DataDisplay dataDisplay)
    {
        this.dataDisplay = dataDisplay;
        
    }
    
    public void setChart(Chart chart)
    {
        this.chart = chart;
        chart.chosenAVC = chosenAVC;
        chart.chosenSVC = chosenSVC;
    }
    
    public void setVisualizer(Visualizer vis)
    {
        this.thisVis = vis;
        if(thisVis.degredation == -1) 
        {
            dataDisplay.ignoreDegCheckBox.setSelected(true);
            dataDisplay.ignoreDegCheckBox.setEnabled(false);
        }
    }
    
    /**
     * This function catches mouse clicks that are not on a node or edge.
     * <p>
     * This function basically just deselects whatever was previously selected by the user.
     * @param e The MouseEvent that created the event 
     */
    @Override
    public void mouseClicked(MouseEvent e)
    {
        itemClicked(null, e);
    }
    
    /**
     * This function handles the highlighting and selecting of nodes.
     * <p>
     * This function is always called when the mouse is clicked whether the user clicked on an item
     * (Which would cause this function to be called by prefuse) or by {@link #mouseClicked(java.awt.event.MouseEvent) }
     * (Which is called when the user clicks but not on an item).
     * @param item The item that was clicked(for the purpose of this program either edge or node.)
     * @param e The mouse event that caused the event(we don't care right click left click does the same thing)
     */
    @Override
    public void itemClicked(VisualItem item, MouseEvent e)
    {
        if(dataDisplay.shouldIgnoreDegredation()) thisVis.degredation = -1;

        
        deSelectPrevItem();//always deSelect whatever was already selected
        
        if(item == null) //if item is null just update dataDisplay and get out of here
        {
            dataDisplay.setUpCharts("no action", null, null);
            return;
        }
        
        if(handleDuplicate(item)) return; //if the same item was handled just return(we just want to deselect if which was called by deSelectPrevItem                  
        
        
        if (item.get("type").equals("node") && SwingUtilities.isLeftMouseButton(e))//type node
        {  
            dataDisplay.setUpCharts("no action", (DynamicMDPState) item.get("stateClass"), null); //update the table with this state
            TableNodeItem node = (TableNodeItem) item;//get the NodeItem
            handleNode(node);//and pass it to handleNode which will take care of the highlighting
        }

        
        else if(item.get("type").equals("edge") && SwingUtilities.isLeftMouseButton(e))//type edge
        {
            ItemClicked = item.getString("action"); //for dataDisplay a string of action name is needed
            dataDisplay.setUpCharts(ItemClicked, (DynamicMDPState)item.get("srcState"), (DynamicMDPState) item.get("resultState"));// update the table for the selected edge
            
            TableEdgeItem edge = (TableEdgeItem) item; //get the edgeItem
            handleEdge(edge);//handleEdge takes care of highlighting the edge
        }  
        
        //bring up the popup menu for nodes
        else if(item.get("type").equals("node") && SwingUtilities.isRightMouseButton(e))
        {

            
            JPopupMenu popup = new JPopupMenu();
            JMenuItem gotoStateMenuItem = new JMenuItem("Go to state");
            JMenuItem expandMenuItem = new JMenuItem("expand");

            ActionListener gotoState = (ActionEvent e1) -> {
                try {
                    goToState(item);
                } catch (FinalStateException ex) {
                    Logger.getLogger(FinalControlListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            };
            
            ActionListener expand = (ActionEvent e1) -> {
                try {
                    expandSelectedNode(item);
                } catch (FinalStateException ex) {
                    Logger.getLogger(FinalControlListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            };
            
            gotoStateMenuItem.addActionListener(gotoState);
            expandMenuItem.addActionListener(expand);
            popup.add(gotoStateMenuItem);
            popup.add(expandMenuItem);
            thisVis.panel.add(popup);
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
        
        //bring up the popup menu for edges
        else if(item.get("type").equals("edge") && SwingUtilities.isRightMouseButton(e))
        {
            JPopupMenu popup = new JPopupMenu();
            JMenuItem executeMenuItem = new JMenuItem("Execute action");
            JMenuItem hideMenuItem = new JMenuItem("Hide Branch");

            ActionListener execute = (ActionEvent e1) -> {
                try {
                    ChangeCurrentState(item, true);
                } catch (FinalStateException ex) {
                    Logger.getLogger(FinalControlListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            };
            
            ActionListener hideBranch = (ActionEvent e1) -> {
                try 
                {
                    handleHideEdge(item);
                }
                catch (ParseException ex)
                {
                    Logger.getLogger(FinalControlListener.class.getName()).log(Level.SEVERE, null, ex);
                } catch (FinalStateException ex) {
                    Logger.getLogger(FinalControlListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            };
            
            executeMenuItem.addActionListener(execute);
            hideMenuItem.addActionListener(hideBranch);
            popup.add(executeMenuItem);
            popup.add(hideMenuItem);
            
            thisVis.panel.add(popup);
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
        
    //end public functions
    //----------------------------------------------------------------------
    //begin private functions
    
    private void expandSelectedNode(Tuple item) throws FinalStateException
    {
        if(getPathFrom( (DynamicMDPState) currentState.get("stateClass"), (DynamicMDPState) item.get("stateClass")) == null && !currentState.get("stateClass").equals(item.get("stateClass"))) return;
        StateNode clickedState = null;
//        for(int i = 0; i < thisVis.tree.nodes.size(); i++)
        {
//            if(item.get("stateClass").equals(thisVis.tree.nodes.get(i).s))
            {
                clickedState = thisVis.tree.nodes.get(item.get("stateClass"));
            }
        }
        
        Node n = thisVis.nodeMap.get(item.get("stateClass"));
//        for(int i = 0; i < thisVis.graph.getNodeCount(); i++)
        {
//            if(thisVis.graph.getNode(i).get("stateClass").equals(item.get("stateClass")))
            {
                GMEAction action = null;
                DynamicMDPState prevStateToClicked = null;
                if(n.getParent() != null)
                {
                    action = (GMEAction) n.getParentEdge().get("CriteriaAction");
                    prevStateToClicked = (DynamicMDPState) n.getParent().get("stateClass");
                }
                
                
                thisVis.addComputeState(clickedState, action, prevStateToClicked, true);
                    
                try 
                {
                    thisVis.setUpData(false);
                    thisVis.updateVisualization();
                    thisVis.rehighlightPath(false, false);
                    //set prevNode and prevEdge to null since they refer to old nodes and edges
                    prevNode = null;
                    prevEdge = null;
                    thisVis.rehighlightPath(false, false);
                    this.resetCurrentState(); //just as prevNode and prevEdge pointed to old things so does current state so change it
                }
                catch (ParseException ex) {
                    Logger.getLogger(FinalControlListener.class.getName()).log(Level.SEVERE, null, ex);
                }
//                break;
            }
        }
    }
    
    private void ChangeCurrentState(Tuple item, boolean updateGraph) throws FinalStateException
    {
        thisVis.temporaryHiddenStates = new HashMap<>();
        thisVis.temporaryHiddentStateNodes = new HashMap<DynamicMDPState, StateNode>();
            clickedAction = (GMEAction) item.get("CriteriaAction");

            double weight = item.getFloat("weight");
            TableEdgeItem edge = (TableEdgeItem) item;


            if(!(weight == 200.0) && edge.getSourceNode().getBoolean("CurrentState"))
            {
                StateNode resultState = null;
//                for(int i = 0; i < thisVis.tree.nodes.size(); i++)
                {
//                    if(thisVis.tree.nodes.get(i).s.equals(edge.getTargetNode().get("stateClass")))
                    {
                        resultState = thisVis.tree.nodes.get(edge.getTargetNode().get("stateClass"));
                    }
                }
                
                System.out.println("executing " + clickedAction.getName());
                
                
                currentState = edge.getTargetItem();
                
                chosenAVC.addAction((DynamicMDPState) edge.getSourceNode().get("stateClass"), (DynamicMDPState) edge.getTargetNode().get("stateClass"), clickedAction);
                double reward = (double) edge.getTargetNode().get("StateReward");
                chosenSVC.addStateValue(reward);
                chart.update();
                try 
                {
                    if(thisVis.chosenStates.isEmpty())
                    {
//                        thisVis.chosenStates.add((State) edge.getSourceNode().get("stateClass"));
                        thisVis.chosenStates.add((DynamicMDPState) edge.getTargetNode().get("stateClass"));
                    }
                    else
                    {
                        thisVis.chosenStates.add(resultState.s);
                    }
                    thisVis.chosenActions.add(clickedAction);
                       
                    thisVis.generateComputeStates(resultState, clickedAction, true);
                    if(updateGraph)
                    {
                        thisVis.setUpData(false);              
                        thisVis.updateVisualization();
                    }
                    thisVis.rehighlightPath(true, false);
                    prevNode = null;
                    prevEdge = null;
                    
                    currentState = (NodeItem) thisVis.vis.getVisualItem("graph.nodes", thisVis.nodeMap.get(thisVis.lastComputeState.thisState.s));
                    
//                    for(int i = 0; i < thisVis.graph.getNodeCount(); i++)
//                    {
//                        if(thisVis.graph.getNode(i).getBoolean("CurrentState"))
//                        {
//                            currentState = (NodeItem) thisVis.vis.getVisualItem("graph.nodes", thisVis.graph.getNode(i));
//                        }
//                    }
                } 
                catch (ParseException ex) {
                    Logger.getLogger(FinalControlListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            
    }
    
    private void goToState(VisualItem item) throws FinalStateException
    {
        ContainerOfActionAndStateSeqence container = getPathFrom((DynamicMDPState) currentState.get("stateClass"), (DynamicMDPState) item.get("stateClass"));
        
        
        if(container != null)
        {
            //this basically steps through every action to thicken it and finally change the current state
            for(int i = 0; i < container.actions.size(); i++) 
            {
                
                Iterator iter = thisVis.nodeMap.get(container.states.get(i)).outEdges();
                
//                TupleSet edges = thisVis.vis.getGroup("graph.edges");
//                Iterator<Tuple> iter = edges.tuples();
                while(iter.hasNext())
                {
                    Tuple thisEdge = (Tuple) iter.next();
                    if(thisEdge.get("CriteriaAction").equals(container.actions.get(i)) && 
                            thisEdge.get("srcState").equals(currentState.get("stateClass"))&&
                            thisEdge.get("resultState").equals(container.states.get(i+1)))
                    {   
                        

                        if(i + 1 == container.actions.size())
                        {
                            ChangeCurrentState(thisVis.vis.getVisualItem("graph.edges", thisEdge), true);
                            break;
                        }
                        else
                        {
                            ChangeCurrentState(thisVis.vis.getVisualItem("graph.edges", thisEdge), true);
                        }
                    }
                }
            }
        }
    }
    
    private void hideNodes(Node n, Node parent)
    {
        if(n.getInDegree() == 1)
        {
            List<Node> childNodes = new ArrayList();
            for(int i = 0; i < n.getChildCount(); i++)
            {
                childNodes.add(n.getChild(i));
            }

            thisVis.temporaryHiddenStates.put((DynamicMDPState) n.get("stateClass"), (DynamicMDPState) n.get("stateClass"));


            for(int i = 0; i < childNodes.size(); i++)
            {
                hideNodes(childNodes.get(i), childNodes.get(i).getParent());
            }
        }
    }
    
    private void handleHideEdge(VisualItem item) throws ParseException, FinalStateException
    {
        if(thisVis.tree.statesTaken.contains((DynamicMDPState) item.get("srcState")) &&
                thisVis.tree.statesTaken.contains((DynamicMDPState) item.get("resultState")))
        {
                    return;
        }
        
        int numberOfChildren = 0;
        Iterator<Edge> children = currentState.childEdges();
        while(children.hasNext())
        {
            children.next();
            numberOfChildren++;
        }
        
        if(numberOfChildren == 0)
        {
            return;
        }
        
        
        
        prevNode = null;
        prevEdge = null;
        dataDisplay.setUpCharts("no action", null, null);
        Iterator edges = thisVis.graph.edges();
        Edge e = null;
        DynamicMDPState src = (DynamicMDPState) item.get("srcState");
        DynamicMDPState result = (DynamicMDPState) item.get("resultState");
        if(getPathFrom((DynamicMDPState) currentState.get("stateClass"), result) == null) return;
        while(edges.hasNext())
        {
            e = (Edge) edges.next();
            if(e.get("srcState").equals(src) && e.get("resultState").equals(result))
            {
                break;
            }
        }
        Node removeNode = e.getTargetNode();

        Connection toBeRemoved = new Connection();
        StateNode newHiddenStateNode = new StateNode();
        newHiddenStateNode.s = (DynamicMDPState) e.getSourceNode().get("stateClass");
        toBeRemoved.action = (GMEAction) e.get("CriteriaAction");
        toBeRemoved.states.add((DynamicMDPState) e.getTargetNode().get("stateClass"));
        newHiddenStateNode.connections.put(toBeRemoved.action.actionName(), toBeRemoved);
        thisVis.temporaryHiddentStateNodes.put(newHiddenStateNode.s, newHiddenStateNode);
        
        hideNodes(removeNode, e.getSourceNode());
            try 
            {
              thisVis.setUpData(false);  
              thisVis.updateVisualization();
              thisVis.rehighlightPath(false, false);
              this.resetCurrentState();
            } 
            catch (ParseException ex) 
            {
                Logger.getLogger(FinalControlListener.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    private void deSelectPrevItem()
    {
        while(!highlightedNodes.isEmpty())//highlighting will change so get rid of here
        {                                //these nodes did not have their stroke size changed so there is no need to setStroke here
            highlightedNodes.get(0).setHighlighted(false);//set false
            highlightedNodes.remove(0);//pop off list
        }
            
            
        if(prevEdge != null)
        {
            prevEdge.setHighlighted(false);   //if prevEdge exists deHighlight it      
        }
        if(prevNode != null) //and some for node
        {                    //this node DID have it's Stroke changed so we need to set it back to zero
            prevNode.setHighlighted(false); 
            if(!prevNode.getBoolean("CurrentState"))
            {
                prevNode.setStroke(new BasicStroke((float) 0.0));//this makes a stroke of size 1(Despite the zero).
            }
            
        }
    }
    /**
     * This function checks for duplicate item and if it does handles getting rid of the previous pointers to items clicked.
     * @param item The visualItem that was clicked
     * @return true or false if it had to handle a case of a duplicate
     */
    private boolean handleDuplicate(VisualItem item)
    {
        NodeItem tempNode = null;
        EdgeItem tempEdge = null;
        
        if(item.get("type").equals("node")) tempNode = (TableNodeItem) item;        
        if(item.get("type").equals("edge")) tempEdge = (TableEdgeItem) item;


        if(tempNode != null)//if the item clicked is a node
        {
            if(tempNode.equals(prevNode))//and is the same as the prev Clicked node
            {
                //get rid of everything
                prevNode = null;
                prevEdge = null;
                dataDisplay.setUpCharts("no action", null, null);
                return true;
            }
        }
            
        else if(tempEdge != null)//same process for edge
        {
            if(tempEdge.equals(prevEdge))
            {
                prevNode = null;
                prevEdge = null;
                dataDisplay.setUpCharts("no action", null, null);
                return true;
            }
        }
        return false;//this statement is reached if both if and else if statement was false(meaning no duplicate)
    }
    
    /**
     * This function handles the highlighting of the node and it's children
     * @param node the clicked node
     */
    private void handleNode(TableNodeItem node)
    {
        if(node.get("CurrentState").equals(false))
        {
            node.setStroke(new BasicStroke(5));//This is the actual node clicked.  To make it different give it a thick stroke
        }


        node.setHighlighted(true);//and highlight it
        prevNode = node;//and then when this function is called again this will be the prev node

        Iterator<Edge> edgeIterator = node.edges();//every edge coming off this node
        while(edgeIterator.hasNext())
        {
            TableEdgeItem edge = (TableEdgeItem) edgeIterator.next();
            if(!edge.getTargetNode().equals((Node) node)) //we are only interested in the edges that point AWAY from the node(result states)
            {
                edge.getTargetItem().setHighlighted(true);//highlight
                highlightedNodes.add(edge.getTargetItem());//and add to the list so the next item clicked can dehighlight this
            }
        }
        prevEdge = null;//if there was an edge selected last set it to null
    }
    
    /**
     * very similar to operation to handleEdge but this handles highlighting the edges
     * @param edge the edge to highlight
     */
    private void handleEdge(TableEdgeItem edge)
    {
        //this code is very self explanitory.
        edge.setHighlighted(true);
        edge.getSourceItem().setHighlighted(true);
        edge.getTargetItem().setHighlighted(true);
        prevEdge = edge;
           
        highlightedNodes.add(edge.getSourceItem());
        highlightedNodes.add(edge.getTargetItem());
            
        prevNode = null;//if node was selected last go ahead and set that to null
    }
    
    private boolean getPathFrom(DynamicMDPState end, Node n, List<GMEAction> actionList, GMEAction act, List<DynamicMDPState> stateList)
    {
        if(actionList == null) actionList = new ArrayList();
        if(act != null) 
        {
            actionList.add(act);
            stateList.add((DynamicMDPState) n.get("stateClass"));
        }
        if(n.get("stateClass").equals(end))  
        {
            return true;
        }
        if(n.getChildCount() == 0) return false;
        Iterator edges = n.childEdges();
       
        List<GMEAction> path = null;
        
        
        
        Iterator iter = n.childEdges();
        int numOfEdges = 0;
        while(iter.hasNext())
        {
            numOfEdges++;
            Edge e = (Edge) iter.next();
            GMEAction nextAction = (GMEAction) e.get("CriteriaAction");
            if(getPathFrom(end, e.getTargetNode(), actionList, act, stateList))
            {
                if(end.equals(e.getTargetNode().get("stateClass")))
                {
                    stateList.add(end);
                }
                actionList.add(nextAction);
                stateList.add((DynamicMDPState) n.get("stateClass"));
                return true;
            }
        }
        
        return false;
    }
    
    public ContainerOfActionAndStateSeqence getPathFrom(DynamicMDPState starting, DynamicMDPState end)
    {
        Node n = null;
        for(int i = 0; i < thisVis.graph.getNodeCount(); i++)
        {
            if(thisVis.graph.getNode(i).get("stateClass").equals(starting))
            {
                n = thisVis.graph.getNode(i);
            }
        }
        List<GMEAction> reversePath = new ArrayList<>();
        List<GMEAction> path = new ArrayList();
        List<DynamicMDPState> statePath = new ArrayList();
        List<DynamicMDPState> reverseStatePath = new ArrayList();
        getPathFrom(end, n, reversePath, null, reverseStatePath);
        
        
        for(int i = reverseStatePath.size() - 1; i >= 0; i--)
        {
            statePath.add(reverseStatePath.get(i));
        }
        
        for(int i = reversePath.size()-1; i >= 0; i--)
        {
            path.add(reversePath.get(i));
        }
        
        
        
        ContainerOfActionAndStateSeqence container = new ContainerOfActionAndStateSeqence();
        container.states = statePath;
        container.actions = path;
        if(statePath.isEmpty()) return null;
        return container;
    }
    
    private void resetCurrentState()
    {
        for(int i = 0; i < thisVis.graph.getNodeCount(); i++)
        {
            if(thisVis.graph.getNode(i).getBoolean("CurrentState"))
            {
                currentState = (NodeItem) thisVis.vis.getVisualItem("graph.nodes", thisVis.graph.getNode(i));
            }
        }
    }
    
    
    class ContainerOfActionAndStateSeqence
    {
        List<GMEAction> actions;
        List<DynamicMDPState> states;
        public ContainerOfActionAndStateSeqence()
        {
            actions = new ArrayList();
            states = new ArrayList();
        }
    }
}
