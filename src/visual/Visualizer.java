/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visual;


import Tree.StateNode;
import Tree.StateTree;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.DataSizeAction;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.VisualItem;

import prefuse.visual.expression.InGroupPredicate;
import prefuse.render.EdgeRenderer;
import BurlapVisualizer.MyController;
import Tree.Connection;
import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import dynamicmdpcontroller.DynamicMDPState;
import dynamicmdpcontroller.Reward;
import dynamicmdpcontroller.actions.GMEAction;
import dynamicmdpcontroller.controllers.FinalStateException;
import java.awt.BasicStroke;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import javax.swing.JPanel;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.data.Tree;
import prefuse.visual.NodeItem;


/**
 * This class is our handle the the visualizer as a whole.  After the state space has been selected this class takes over execution.
 * @author Justin Lewis
 */
public final class Visualizer 
{
    Tree graph; //this is the major data type of prefuse(contains the nodes and edges and other info)
    Visualization vis;
    Display d;
    StateTree tree;
    HashMap<DynamicMDPState, StateNode> nodeList;
    private DataDisplay dataDisplay;
    FinalControlListener tooltipHandler;
    List<String> allAtrribs;
    Reward reward;
    JFrame frame;
    MyController thisController;
    FinalControlListener mouse;
    List<VisualItem> edgeItems; //this is a list of the edge items from init to target state
    List<Edge> edges;
    List<Node> nodes;
    StateValueContainer stateValueContainer;
    ActionValueContainer actionValueContainer;
    Chart chart;
    List<ComputeState> computableStates;
    List<ComputeState> oldComputeStates;
    double degredation;
    ComputeState lastComputeState; //this acts as our computeState that is our currentState
    List<DynamicMDPState> chosenStates;
    List<GMEAction> chosenActions;
    ActionList color;
    ActionList layout;
    ActionList repaint;
    List<List<DynamicMDPState>> visibleStates = new ArrayList();
    List<List<Boolean>> undoStates = new ArrayList(); /////unimplemented at the moment
    List<List<GMEAction>> visibleActions = new ArrayList();
    HashMap<DynamicMDPState, DynamicMDPState> temporaryHiddenStates = new HashMap<>();
    HashMap<DynamicMDPState, StateNode> temporaryHiddentStateNodes = new HashMap<>();
    HashMap<DynamicMDPState, DynamicMDPState> dontComputeStates = new HashMap<>();
    
    JPanel panel;
    
    int[] Originals_pallette;
    int[] s_pallette;
    int[] Originale_pallette;
    int[] e_pallette;
    
    DataColorAction fill;
    DataColorAction dcaEdges;
    DataColorAction arrowColor;
    
    
    HashMap<State, Node> nodeMap;
    boolean pinkNode;
    boolean containsSubOptimalPath;
    boolean containsNonOptimalPath;
    int index;
    
    
    
    /**
     * This function will close the {@link visual.DataDisplay} and this class.
     */
    public void closeWindows()
    {
        if(frame != null && dataDisplay != null)
        {
            frame.dispose();
            dataDisplay.close(); 
            chart.close();
            
        }

    }
    
    /**
     * This thread sets up the actual visualization with the fed in data.
     * <p>
     * When this function ends all that is left running is control listeners and prefuse threads.
     * There is nothing more we must do.
     * @param tree tree data structure you want to visualize.
     * @param allAttribs List of string of attributes each state has
     * @param actions List of every possible action in the MDP
     * @param controller The underlying controller for this MDP
     * @throws IOException
     * @throws ParseException 
     */
    public Visualizer(StateTree tree, List<String> allAttribs, List<GMEAction> actions, MyController controller, double degredation, int index) throws IOException, ParseException, FinalStateException
    {
        this.index = index;
        nodeMap = new HashMap<>();
        thisController = controller;
        actionValueContainer = new ActionValueContainer(thisController);
        stateValueContainer = new StateValueContainer(actionValueContainer);
        
        Originals_pallette = new int[4];
        Originals_pallette[0] = ColorLib.rgb(0,0,255);
        Originals_pallette[1] = ColorLib.rgb(255, 0, 0);
        Originals_pallette[2] = ColorLib.rgb(102,51,153);
        Originals_pallette[3] = ColorLib.rgb(255,105,180);
        
        Originale_pallette = new int[2];
        Originale_pallette[0] = ColorLib.rgb(0,255,0);
        Originale_pallette[1] = ColorLib.rgb(255,0,0);
        

        
        this.computableStates = new ArrayList();
        this.oldComputeStates = new ArrayList();
        this.chosenStates = new ArrayList();
        this.chosenStates.add(tree.initialState);
        this.chosenActions = new ArrayList();
        
        this.tree = tree;
        this.nodeList = this.tree.getNodes();
        this.allAtrribs = allAttribs;
        thisController = controller;
        this.degredation = degredation;
        panel = new JPanel();
        mouse = new FinalControlListener(thisController);
       
        //this sets up the initial visualizer
        setUpData(true);
        setUpVisualization();
        
        //this is the only time these functions get called in the whole program
        setUpRenderers();
        setUpActions();
        setUpDisplay();
        chart = new Chart(stateValueContainer, actionValueContainer);
        mouse.setChart(chart);
}

    
    
    /**
     * This is the last function in the program that gets called that sets up the windows for viewing.
     */
    public void setUpDisplay()
    {
        dataDisplay = new DataDisplay(tree, this.allAtrribs, thisController,edgeItems, mouse, index); //set up the dataDisplay(mouse is needed for buttons)
        dataDisplay.setUpCharts("no action", null, null);//set charts up with nothing selected
        mouse.setDataDisplay(dataDisplay);//and let mouse have control of dataDisplay(To set up tables when user clicks)
        mouse.setVisualizer(this);
        mouse.currentState = (NodeItem) vis.getVisualItem("graph.nodes", nodes.get(0));//set mouses current state to the intial state
                        
        d = new Display(vis);
        d.setSize(900, 900);
        d.addControlListener(new DragControl());
        d.addControlListener(new PanControl());
        d.addControlListener(new ZoomControl());
        d.addControlListener(new WheelZoomControl());
        d.addControlListener(mouse); //controls when you click node or edge
        
        frame = new JFrame("Burlap Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(d);
        frame.pack();
        frame.setVisible(true);
        
        //these layouts were defined in setUpActions()
        vis.run("color");
        vis.run("layout");
    }
    

    
    
    /**
     * This function sets up the visualization with the given graph.
     * <p>
     * The visualizer in prefuse handles things like color rather than the raw data like the graph.
     */
    public void setUpVisualization()
    {       
        vis = new Visualization();

        Table n = graph.getNodeTable();
        Table e = graph.getEdgeTable();
        Graph g_new = new Graph(n, e, true); //this was required to give edges directions
        

        vis.add("graph", g_new);
        setItems();//now that vis has been created we can deal with items.
    }
    
    public void updateVisualization()
    {
//        vis.removeAction("color"); //the old color has been changed so remove old color
        vis.putAction("color", color); //and replace with new color
        vis.putAction("layout", layout);
        setItems(); //handles stuff like giveing stokes and the optimal path
        //run the actions thate were cancelled in setUpData
        vis.run("color"); 
        vis.run("layout");
    }
    
    
    /**
     * This handles how the nodes and edges are drawn in prefuse.
     * <p>
     * We gave some edges labels and all nodes a label.  We also made the nodes oval shapped which was handled in this function.
     */
    public void setUpRenderers()
    {
        FinalRenderer r = new FinalRenderer();
        EdgeRenderer er = new EdgeRenderer(Constants.EDGE_TYPE_LINE, prefuse.Constants.EDGE_ARROW_FORWARD);
       
        DefaultRendererFactory drf = new DefaultRendererFactory();
        drf.setDefaultRenderer(r);
        drf.setDefaultEdgeRenderer(er);
        

        
        // We now have to have a renderer for our decorators.
        LabelRenderer actionNameLabel = new LabelRenderer("ActionName");
        drf.add(new InGroupPredicate("ActionName"), actionNameLabel);
        drf.add(new InGroupPredicate("stateRewards"), new LabelRenderer("StateReward"));
        

    
        // -- Decorators responsible for both types of labels(edges and nodes)
        //edge labels
        final Schema DECORATOR_SCHEMA = PrefuseLib.getVisualItemSchema();
        DECORATOR_SCHEMA.setDefault(VisualItem.INTERACTIVE, false); 
        DECORATOR_SCHEMA.setDefault(VisualItem.TEXTCOLOR, 
                               ColorLib.rgb(0, 0, 0)); 
        DECORATOR_SCHEMA.setDefault(VisualItem.FONT, 
                               FontLib.getFont("Goblin One",12));
        
        //node labels
        final Schema DECSchema = PrefuseLib.getVisualItemSchema();
        DECSchema.setDefault(VisualItem.INTERACTIVE, false);
        DECSchema.setDefault(VisualItem.TEXTCOLOR, ColorLib.rgb(0, 200, 0));
        DECSchema.setDefault(VisualItem.FONT, FontLib.getFont("Tahoma", 12));
        
        //add decorators to vis
        vis.addDecorators("ActionName", "graph.edges", DECORATOR_SCHEMA);
        vis.addDecorators("stateRewards", "graph.nodes", DECSchema);
        
      
        // This Factory will use the ShapeRenderer for all nodes.
        vis.setRendererFactory(drf);
    }
    
    
    
    
    /**
     * These actions define how color and size work in our visualization.
     */
    public void setUpActions()
    {
        //these palletes are used in the action colors.  They change based on what is visible
        setStatePallette(true);
        setEdgePallette(true);
        
        
        //this action handles how nodes are colored
        fill = new DataColorAction("graph.nodes", "nodeInfo", Constants.ORDINAL, VisualItem.FILLCOLOR, s_pallette);   
        fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255,255, 0));
        
        //this action handles how edges are colored
        dcaEdges = new DataColorAction("graph.edges", "inPath",  Constants.NOMINAL, VisualItem.STROKECOLOR, e_pallette);
        dcaEdges.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 255, 0));
        arrowColor = new DataColorAction("graph.edges", "inPath",Constants.NOMINAL, VisualItem.FILLCOLOR, e_pallette);
        arrowColor.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 255, 0));
        //this action handles how thick edges are
        DataSizeAction edgeThick = new DataSizeAction("graph.edges", "weight");
        edgeThick.setMinimumSize(20); //strange how this affects thickness and not the edge renderer....but whatever
        
        
        //list of actions that define color...let it run indefinatly
        color = new ActionList(Activity.INFINITY);
        color.add(fill);
        color.add(dcaEdges);
        color.add(arrowColor);
        color.add(edgeThick);

        layout = new ActionList(Activity.INFINITY);

        NodeLinkTreeLayout treeLayout = new NodeLinkTreeLayout("graph", Constants.ORIENT_LEFT_RIGHT, 400, 100, 120);
        treeLayout.setLayoutAnchor(new Point2D.Double(25,300));
            
        //layout just contains the tree layout, decorators and a repaint that helps when we 
        //modify the graph in the future
        layout.add(treeLayout);
        layout.add(new FinalDecoratorLayout("stateRewards"));
        layout.add(new FinalDecoratorLayout("ActionName"));
        layout.add(new RepaintAction());
       
        vis.putAction("color", color);
        vis.putAction("layout", layout);
    }
    
    /**
     * This function defines what data prefuse will be working with such as
     * how nodes connect to other nodes.
     * @throws ParseException 
     */
    public void setUpData(boolean init) throws ParseException, FinalStateException
    {
        //this checks to see 
        if(lastComputeState != null && thisController.isTerminal(index, lastComputeState.thisState.s)) this.closeWindows();
        
        //the following variables need to be refreashed every time the
        //the visualizer refreashes
        nodes = new ArrayList<>();
        edgeItems = new ArrayList<>();
        edges = new ArrayList<>();
        pinkNode = false;
        containsNonOptimalPath = false;
        containsSubOptimalPath = false;
        
               
        //set up graph and set all columns only once
        //if you set graph to new Tree visualization screws up
        //and then so does renderer and...you get the picture
        if(init)
        {
            graph = new Tree();
            graph.addColumn("state", String.class);
            graph.addColumn("reward", Double.class);
            graph.addColumn("nodeInfo", Integer.class);
            graph.addColumn("action", String.class);
            graph.addColumn("inPath", Integer.class);
            graph.addColumn("weight", float.class);
            graph.addColumn("type", String.class);
            graph.addColumn("stateClass", DynamicMDPState.class);
            graph.addColumn("srcState", DynamicMDPState.class);
            graph.addColumn("resultState", DynamicMDPState.class);
            graph.addColumn("CriteriaAction", GMEAction.class);
            graph.addColumn("StateReward", Double.class);
            graph.addColumn("ActionName", String.class);
            graph.addColumn("CurrentState", boolean.class);
            generateComputeStates(tree.initialNode, null, true);
        }
        else
        {
            //we want to empty the graph if it has already been set up
            //but prefuse does not like it when these actions try to run on
            //an empty graph so we will stop them
            vis.cancel("color");
            vis.cancel("layout");
            //we remove them because these actions have a tendency to change
            vis.removeAction("color");
            vis.removeAction("layout");
            graph.clear();
        }
        
        visibleStates = new ArrayList();
        visibleActions = new ArrayList();
        
        Hashtable<String, GMEAction> definedActions = thisController.getActionMap(index);
        
        //largestAllowedReward is only used if degredation is set
        double optimalReward = thisController.getReward(tree.actionsTaken, tree.statesTaken);
        double largestAllowedReward = optimalReward * (1 +(degredation/100));
        
        //The below loop takes every computeable state and puts them in arrays
        for(int i = 0; i < computableStates.size(); i++)
        {
            List<DynamicMDPState> onePath = computableStates.get(i).getEffectiveStateList();
            List<GMEAction> oneActionPath = computableStates.get(i).getEffectiveActionList();

            double thisReward = thisController.getReward(oneActionPath, onePath);
            //if degredation is set and it meets it
            if(degredation >= 0 && thisReward >= largestAllowedReward)
            {
                visibleStates.add(onePath);
                visibleActions.add(oneActionPath);   
            }
            //else the degredation is not set which means the total reward does not matter
            else
            {
                visibleStates.add(onePath);
                visibleActions.add(oneActionPath);
            }
        }
        
        setUpNodes(init);
//        setPrefuseNodeConnections();
        setDefaultEdgeData();
        handleSubPathToTarget();
        handlePathToTarget();
        //if this is not the first time to visualize then reset the pallettes for state and edge coloring
        if(!init)
        {
            setStatePallette(false);
            setEdgePallette(false);
        }
    }
    
    //end public functions.
    //------------------------------------------------------------------------------------------------------
    //begin of private functions
    
    /**
     * sets the items for edgeItems and gives every node a black stroke
     */
    private void setItems()
    {
        //give every node black stroke
        Iterator allNodes = graph.nodes();
        while(allNodes.hasNext())
        {
            Node tempNode = (Node) allNodes.next();
            VisualItem item = vis.getVisualItem("graph.nodes", tempNode);
            item.setStrokeColor(ColorLib.rgb(0, 0, 0));
            item.setStroke(new BasicStroke(0));
            if(item.getBoolean("CurrentState")) vis.getVisualItem("graph.nodes", tempNode).setStroke(new BasicStroke(10));
        }
    }
    
    
    private void setUpOptimalNodes(Node n) throws FinalStateException
    {
        for(int i = 0; i < tree.statesTaken.size(); i++)
        {
            n.set("type", "node");
            n.set("state", thisController.getStateString(tree.statesTaken.get(i))); 
            n.set("stateClass", tree.statesTaken.get(i));
            double stateValueFunction = thisController.getV(index, tree.stateNodesTaken.get(i).s);
            double finalStateValueFunction = (double) Math.round(stateValueFunction * 100000) / 100000; //the math.round goes to 5 places(up to the decimal point)
            n.set("StateReward", finalStateValueFunction);
            nodes.add(n);
            nodeMap.put(tree.statesTaken.get(i), n);
            Node temp = n;
                
            //indexing required this type of statement since I wanted the last node to NOT have an edge
            //hence it being a "final" state
            if(i < tree.statesTaken.size() - 1)
            {
                n = graph.addChild(n);
                Edge edge = graph.getEdge(temp, n);
                edge.set("srcState", tree.statesTaken.get(i));//set the edges source state
                edge.set("resultState", tree.statesTaken.get(i + 1));//and target state
                edge.set("action", tree.actionsTaken.get(i).getName());
                edge.set("CriteriaAction", tree.actionsTaken.get(i));
                edge.set("ActionName", tree.actionsTaken.get(i).getName());
                edge.set("reward", thisController.getReward(tree.actionsTaken.get(i), tree.statesTaken.get(i)));
            }
        }
    }
    
    private int setEdgesBetweenPreExistingNodes(Node n, int i , int j)
    {

        DynamicMDPState prevState;
        while(j < visibleStates.get(i).size()-1 && nodeMap.get(visibleStates.get(i).get(j+1)) != null && nodeMap.get(visibleStates.get(i).get(j)) != null)
        {
            prevState = visibleStates.get(i).get(j);
            Edge e = graph.getEdge(nodeMap.get(visibleStates.get(i).get(j)), nodeMap.get(visibleStates.get(i).get(j+1))); //if e is not null it means this case has been handled before
                                                     //although this case seems rare it happens sometimes
            if(e == null && prevState != null)
            {   
                e = graph.addEdge(nodeMap.get(visibleStates.get(i).get(j)), nodeMap.get(visibleStates.get(i).get(j+1)));
                e.set("srcState", prevState);//set the edges source state
                e.set("resultState", visibleStates.get(i).get(j+1));//and target state
                e.set("action", visibleActions.get(i).get(j).getName());
                e.set("CriteriaAction", visibleActions.get(i).get(j));
                e.set("ActionName", visibleActions.get(i).get(j).getName());
                e.set("reward", thisController.getReward(visibleActions.get(i).get(j), visibleStates.get(i).get(j)));   
            }
            j++;  //since we handled a state go ahead and increment j
                            //I would have liked to have used continue but since
                              //we are looping over the graph and not visibleStates
                              //continue would not have the desired effect
                              //the following if statement handles cases where j goes over 
                              //visibleState.size()
            if(j >= visibleStates.get(i).size())//if we go over break
            {
                return -1;
            }
            n = nodeMap.get(visibleStates.get(i).get(j));
        }
        if(j > visibleStates.get(i).size()) return -1;
        return j;
    }
    
    /**
     * create nodes for each state
     */
    private void setUpNodes(boolean init) throws FinalStateException
    {      
        nodeMap = new HashMap<>();
        if(init)    graph.addRoot().set("CurrentState", true); //this adds the root and sets a needed column
        else      graph.addRoot();
        
        Node n = graph.getRoot();
        
        setUpOptimalNodes(n);

        //end optimal path loop
        
        
        //paths other than the otimal one
        //visibleStates.get(i) represents a list of states that represent one path
        //visibleAction.get(i) represents a list of actions that represent one path
        //both lists are parrell meaning the to go from state 1 to 2 action 1 was applied and so forth
        for(int i = 0; i < visibleStates.size(); i++)
        {
            n = graph.getRoot();
            DynamicMDPState prevState = null;
            int numOfNodes = visibleStates.get(i).size();
            for(int j = 0; j < numOfNodes; j++)
            {   
                int newJ = setEdgesBetweenPreExistingNodes(n, i, j);
                if(newJ < 0) continue;
                j = newJ;
                n = nodeMap.get(visibleStates.get(i).get(j));
                
                


                boolean add = true; //this checks for indiv edges(such as that between a non-optimal node
                                        //to an optimal one where if hide branch was used it would have no node
                                        //to hide but just a single edge.  This is the fix for that
                if( !(j < visibleStates.get(i).size() - 1))
                {
                    continue;
                }
                else if(temporaryHiddenStates.get(visibleStates.get(i).get(j + 1)) != null)
                {
                    add = false;
                }
                                
                    
//                n = nodeMap.get(visibleStates.get(i).get(j));
                Node temp = n; //temp keeps up with n's last node value
                if(add) //if this is the first or is supposed to be added then do add
                {
                    n = graph.addChild(n);
                }
                else
                {
                    break;
                }
                
                //handle the edge between the prev node and the new just added node
                Edge edge = graph.getEdge(temp, n);
                edge.set("srcState", visibleStates.get(i).get(j));//set the edges source state
                edge.set("resultState", visibleStates.get(i).get(j+1));//and target state
                edge.set("action", visibleActions.get(i).get(j).getName());
                edge.set("CriteriaAction", visibleActions.get(i).get(j));
                edge.set("ActionName", visibleActions.get(i).get(j).getName());
                edge.set("reward", thisController.getReward(visibleActions.get(i).get(j), visibleStates.get(i).get(j)));
                
                //set various info about this new added node
                n.set("type", "node");
                n.set("state", thisController.getStateString(visibleStates.get(i).get(j+1))); 
                n.set("stateClass", visibleStates.get(i).get(j+1));  
                prevState = visibleStates.get(i).get(j+1);
                double stateValueFunction = thisController.getV(index, visibleStates.get(i).get(j+1));
                double finalStateValueFunction = (double) Math.round(stateValueFunction * 100000) / 100000; //the math.round goes to 5 places(up to the decimal point)
                n.set("StateReward", finalStateValueFunction);
                //put the new node in our big hashMap
                nodeMap.put(visibleStates.get(i).get(j+1), n);
                
                

                if(thisController.isTerminal(index, visibleStates.get(i).get(j+1)))
                {
                    n.set("nodeInfo", 0); //blue node
                }
                else if(visibleStates.get(i).get(j+1).equals(tree.initialState))
                {
                    n.set("nodeInfo", 1); //red node
                }
                else
                {
                    n.set("nodeInfo", 3); //pink node
                    pinkNode = true;
                }
            }
        } 
    }
    /**
     * exactly what the function names says. It creates the connections between nodes in the graph.
     */
    private void setPrefuseNodeConnections()
    {
        int counter = 0;
        
        for(int i = 0; i < tree.actionsTaken.size(); i++)
        {
            Node n1 = graph.getNode(counter);
            Node n2 = graph.getNode(counter + 1);
          
            DynamicMDPState currentState = tree.statesTaken.get(i);
            DynamicMDPState nextState = tree.statesTaken.get(i + 1);
            
            if(thisController.isTerminal(index, nextState))
            {
                n2.set("nodeInfo", 0); //blue node
            }
            if(currentState.equals(tree.initialState))
            {
                n1.set("nodeInfo", 1); //red node
            }
            else
            {
                n1.set("nodeInfo", 3); //pink node
            }
            counter++;
        }
    }
    
    /**
     * This function just sets the default data for every edge.
     * <p>
     * For edges that are actions to the target state the weight and inPath parameters will change.
     */
    private void setDefaultEdgeData()
    {
        for(int i = 0; i < graph.getEdgeCount(); i++)
        {
            Edge e = graph.getEdge(i);
            e.set("type", "edge");
            e.set("weight", 1.0);
            e.set("inPath", 1);
        }
    }
    
    
    
    private void handleSubPathToTarget()
    {
        ComputeState cs = lastComputeState;
        if(tree.statesTaken.contains(cs.thisState.s)) return; //if we are in a purple node the optimal path can be taken
        List<DynamicMDPState> subOptimalPath = cs.convertToStateList();
        
        int stop = subOptimalPath.size() - 1;
        
        
        
        for(int i = 0; i < stop; i++)
        {
            if(nodeMap.get(subOptimalPath.get(i)) == null || nodeMap.get(subOptimalPath.get(i+1)) == null) break;
            Edge e = graph.getEdge(nodeMap.get(subOptimalPath.get(i)), nodeMap.get(subOptimalPath.get(i+1)));
            if(e == null) break;
            e.set("inPath", 2);
            containsSubOptimalPath = true;
        }
    }
    
    /**
     * This function sets data needed for the nodes and edges from the initial state to the target state
     */
    private void handlePathToTarget() throws FinalStateException
    {
        for(int i = 0; i < tree.stateNodesTaken.size() - 1; i++)
        {
            //the following lines work because the indexes inside the graph
            //are the same as the indexes in the tree
            
            //The reason this is is because I added the optimal path first
            StateNode src = tree.stateNodesTaken.get(i);
            StateNode target = tree.stateNodesTaken.get(i+1);
            int indexSrc = i;
            int indexTarget = i+1;
            Node nodeSrc = graph.getNode(indexSrc);
            Node nodeTarget = graph.getNode(indexTarget);
            
            stateValueContainer.addStateValue(thisController.getV(index, src.s));
            actionValueContainer.addAction(src.s, target.s, src.checkIfResultState(target.s));
            
            if(i == 0) nodeSrc.set("nodeInfo", 1);
            if(!thisController.isTerminal(index, target.s)) nodeTarget.set("nodeInfo", 2); //purple nodes
            else nodeTarget.set("nodeInfo", 0);
            if(!nodeSrc.equals(nodeTarget)) //if the result state is the same as the src then nothing needs to happen
            {
                Edge e = graph.getEdge(nodeSrc, nodeTarget); //get the edge between the nodes
            
                if(e != null) //and change it accordingly
                {
                    e.set("inPath", 0);  
                    e.set("weight", 1.0);
                    edges.add(e); //edges is a list of the edges from initial to target
                }
            }
            System.out.println(nodeSrc.get("nodeInfo"));
                        
        }//end of loop
        stateValueContainer.addStateValue(0); //final state has 0 value
    }
    
    //below function was an early attempt at looking at state tree
    private void generateComputeStates(ComputeState prevState) throws FinalStateException
    {
        for(int i = 0; i < prevState.thisState.connections.size(); i++)
        {
            for(int j = 0; j < prevState.thisState.connections.get(i).nodes.size(); j++)
            {
                StateNode nextNode = findInTree(prevState.thisState.connections.get(i).nodes.get(0).s);
                ComputeState nextState = new ComputeState();
                for(int k = 0; k < prevState.prevStates.size(); k++)
                {
                    nextState.prevStates.add(prevState.prevStates.get(k));
                }
                for(int k = 0; k < prevState.prevActions.size(); k++)
                {
                    nextState.prevActions.add(prevState.prevActions.get(k));
                }
                boolean shouldAdd = true;
                nextState.thisState = nextNode;
                for(int k = 0; k < computableStates.size(); k++)
                {
                    if(computableStates.get(k).thisState.equals(nextState.thisState))
                    {
                        shouldAdd = false;
                    }
                }
                nextState.prevStates.add(prevState.thisState);

                nextState.prevActions.add(prevState.thisState.connections.get(i).action);
                if(shouldAdd)
                {
                    this.computableStates.add(nextState);
                }
                nextState.ea = thisController.getOptimalPathFrom(0, nextState.thisState.s);
                generateComputeStates(nextState);
            }
        }
        
    }
    
    
    
    
    private void handleExpansionComputeState(ComputeState newCompute, Connection connect, DynamicMDPState initState)
    {
                            DynamicMDPState nextState = newCompute.prevStates.get(newCompute.prevStates.size() - 1).s;
                    newCompute.prevStates.remove(newCompute.prevStates.size() - 1);
                    FinalControlListener.ContainerOfActionAndStateSeqence cont = mouse.getPathFrom(nextState, initState);
                    if(cont != null)
                    {
                        for(int a = 0; a < cont.states.size(); a++)
                        {
                            newCompute.prevStates.add(tree.nodes.get(cont.states.get(a)));
                        }
                        for(int a = 0; a < cont.actions.size(); a++)
                        {
                            newCompute.prevActions.add(cont.actions.get(a));
                        }
                    }
                    
                    else
                    {
                        newCompute.prevStates.add(lastComputeState.thisState);
                    }
                    newCompute.prevActions.add(connect.action);
    }
    
    /**
     * This is the method that handles adding states by the expand feature
     * @param initState
     * @param prevAction
     * @param prevState 
     */
    public void addComputeState(StateNode initState, GMEAction prevAction, DynamicMDPState prevState, boolean isExpanding) throws FinalStateException
    {   
        for(Connection connect : initState.connections.values())
        {
            for(int j = 0; j < connect.nodes.size(); j++)
            {
                boolean found = false;
                ComputeState newCompute;
                ComputeState oldCompute = findInAlreadyComputedStates(connect.nodes.get(j));
                if(oldCompute != null)
                {
                    found = true;
                }
                

                newCompute = new ComputeState();
                for(int a = 0; a < chosenStates.size(); a++)
                {
                    newCompute.prevStates.add(tree.getNodeForState(chosenStates.get(a)));
                }
                for(int a = 0; a < chosenActions.size(); a++)
                {
                    newCompute.prevActions.add(chosenActions.get(a));
                }
                    
                    
                if(isExpanding)
                {
                    handleExpansionComputeState(newCompute, connect, initState.s);
                }

                else
                {                             
                    newCompute.prevActions.add(connect.action);
                }
                    //------------------------------------------
                    //end filling prevStates and prevActions
                
                
                                
                newCompute.thisState = connect.nodes.get(j);
                
                if(found == false)
                {
                    newCompute.ea = thisController.getOptimalPathFrom(this.index, connect.nodes.get(j).s);
                    newCompute.ea.stateSequence.remove(0); //the first stateSequence is the current state which is already included so get rid of it
                }
                else
                {
                    mergeOldComputetoNewCompute(newCompute, oldCompute);
                }
                
                newCompute.validEa = false;


                //get reward of proposed path
                //I had to create a new list proposedPath so that I could group all
                //the actions up without messing up the integrity of each seperate list
                //in the compute state.  If only java provided a easy to use deep copy method.....sigh.....
                List<GMEAction> proposedPath = new ArrayList();
                List<DynamicMDPState> proposedPathStates = new ArrayList();
                for(int k = 0; k < newCompute.prevActions.size(); k++)
                {
                    proposedPath.add(newCompute.prevActions.get(k));
                }
                for(int k = 0; k < newCompute.ea.actionSequence.size(); k++)
                {
                    GMEAction plannedAct = thisController.getActionMap(index).get(newCompute.ea.actionSequence.get(k).actionName());
                    proposedPath.add(plannedAct);
                }
                for(int k = 0; k < newCompute.prevStates.size(); k++)
                {
                    proposedPathStates.add(newCompute.prevStates.get(k).s);
                }
                proposedPathStates.add(newCompute.thisState.s);
                for(int k = 0; k < newCompute.ea.stateSequence.size();k++)
                {
                    proposedPathStates.add((DynamicMDPState) newCompute.ea.stateSequence.get(k));
                }
                
                double totalReward = thisController.getReward(proposedPath, proposedPathStates);
                
                double optimalReward = thisController.getReward(tree.actionsTaken, tree.statesTaken);
                double largestAllowedReward = optimalReward * (1 +(degredation/100));
                
                boolean alreadyComputed = false;
                
                List<DynamicMDPState> tempList = newCompute.convertToStateList();
                for(int a = 0; a < tempList.size(); a++)
                {
                    if(dontComputeStates.get(tempList.get(a)) != null)
                    {
                        alreadyComputed = true;
                    }
                }
                //remember if degredation is < 0 it is not set
                if((totalReward > largestAllowedReward && !alreadyComputed) || this.degredation < 0)
                {
                        computableStates.add(newCompute);   
                        oldComputeStates.add(newCompute);
                }
                else
                {
                    dontComputeStates.put(newCompute.thisState.s, newCompute.thisState.s);
                }
            }
        }
    }
    
    private void mergeOldComputetoNewCompute(ComputeState newCompute, ComputeState oldCompute)
    {
        if(newCompute.thisState.equals(oldCompute.thisState)) 
        {
            newCompute.ea = new Episode();
            for(int i = 0; i < oldCompute.ea.actionSequence.size(); i++)
            {
                newCompute.ea.actionSequence.add(oldCompute.ea.actionSequence.get(i));
            }
            for(int i = 0; i < oldCompute.ea.stateSequence.size(); i++)
            {
                newCompute.ea.stateSequence.add(oldCompute.ea.stateSequence.get(i));
            }
        }
    }
    
    
    
/**
 * This is called after the current state moves.
 * @param initState
 * @param prevAction
 * @param changeCurrentState 
 */
    public void generateComputeStates(StateNode initState, GMEAction prevAction, boolean changeCurrentState) throws FinalStateException 
    {
        DynamicMDPState prevState = null;
        for(int i = 0; i < oldComputeStates.size(); i++)
        {
            oldComputeStates.get(i).validEa = false;
        }
        if(changeCurrentState)
        {
            computableStates = new ArrayList<>();
        }
        
        
        //if null we set lastComputeState to initialState and ea to the very first ea
        if(lastComputeState == null) 
        {
            lastComputeState = new ComputeState();
            lastComputeState.thisState = initState;
            lastComputeState.ea = thisController.getEpisode(0);
            List<State> stateSeq = new ArrayList();
            List<Action> actionSeq = new ArrayList();
            for(int i = 1; i < lastComputeState.ea.stateSequence.size(); i++)
            {
                stateSeq.add(lastComputeState.ea.stateSequence.get(i));
            }
            for(int i = 0; i < lastComputeState.ea.actionSequence.size(); i++)
            {
                actionSeq.add(lastComputeState.ea.actionSequence.get(i));
            }
            lastComputeState.ea.stateSequence = stateSeq;
            lastComputeState.ea.actionSequence = actionSeq;
        }
        else if(changeCurrentState)
        {
            lastComputeState.prevStates.add(lastComputeState.thisState);
            lastComputeState.prevActions.add(prevAction);
            lastComputeState.thisState = initState;
            
            
            if(!lastComputeState.ea.stateSequence.isEmpty() && lastComputeState.thisState.s.equals(lastComputeState.ea.stateSequence.get(0)))
            {
                lastComputeState.ea.stateSequence.remove(0);
                lastComputeState.ea.actionSequence.remove(0);
            }
            
            else if(tree.stateNodesTaken.contains(initState))
            {
                lastComputeState.ea = new Episode(); //the optimal path is always shown so no need for
                                                             //a real EpisodeAnaylsis
            }
            
            else
            {
                ComputeState temp = findInAlreadyComputedStates(initState);
                mergeOldComputetoNewCompute(lastComputeState, temp);
                //keep the below comments. still testing
//                if(temp != null)
//                {
//                    mergeOldComputetoNewCompute(lastComputeState, temp);
//                }
//                else
//                {
////                    System.out.println("recomputing!!!");
//                    lastComputeState.ea = thisController.getOptimalPathFrom(0, initState.s);// thisController.getOptimalPathFrom(initState.s);//this line must change
//                    lastComputeState.ea.stateSequence.remove(0);
//                }
            }
        }
        
        lastComputeState.validEa = true; //the ea from the current state should always be visible(sub optimal path)
        
        if(lastComputeState.prevStates.size() > 1)
        {
            prevState = lastComputeState.prevStates.get(lastComputeState.prevStates.size() - 1).s;
        }
        
        if(!lastComputeState.ea.stateSequence.isEmpty() && lastComputeState.thisState.s.equals(lastComputeState.ea.stateSequence.get(0)))
        {
            lastComputeState.ea.actionSequence.remove(0);
            lastComputeState.ea.stateSequence.remove(0);
        }
        addComputeState(initState, prevAction, prevState, false);
        computableStates.add(lastComputeState);
        lastComputeState.validEa = true;

    }
    
    
    /**
     * if a state is found and already has a computed ea find it here and use that 
     * sequence rather than recomputing
     * @param s
     * @return 
     */
    private ComputeState findInAlreadyComputedStates(StateNode s)
    {
        boolean inEA = false;
        int index = 0;
        int subIndex = 0;
        
        for(int i = 0; i < oldComputeStates.size(); i++)
        {
            ComputeState testState = oldComputeStates.get(i);
            if(testState.thisState.equals(s))
            {
                return testState;
            }
            
            else if(testState.ea.stateSequence.contains(s.s))
            {
                inEA = true;
                index = i;
                subIndex = testState.ea.stateSequence.indexOf(s.s) + 1;
                break;
            }
        }
        ComputeState newCompute = new ComputeState();
        
        if(inEA)
        {
            //fill prev states
            for(int i = 0; i < chosenStates.size() - 1; i++)
            {
                newCompute.prevStates.add(tree.getNodeForState(chosenStates.get(i)));
            }
            newCompute.thisState = tree.getNodeForState(chosenStates.get(chosenStates.size() - 1)); //last element of chosenStates is currentState
            
            //fill prevActions
            for(int i = 0; i < chosenActions.size(); i++)
            {
                newCompute.prevActions.add(chosenActions.get(i));
            }
            
            newCompute.ea = new Episode();
            
            //fill the ea with the proper info
            for(int i = subIndex; i < oldComputeStates.get(index).ea.stateSequence.size(); i++)
            {
                newCompute.ea.stateSequence.add(oldComputeStates.get(index).ea.stateSequence.get(i));
            }
            
            for(int i = subIndex; i < oldComputeStates.get(index).ea.actionSequence.size(); i++)
            {
                newCompute.ea.actionSequence.add(oldComputeStates.get(index).ea.actionSequence.get(i));
            }
             


            //no need to compute reward since all computed states that are added
            //already meet the reward criteria
            return newCompute;
        }
        
        
        return null;
    }
    
    
//    private double getRewardForComputeState(ComputeState compute)
//    {
//        List<GMEAction> actions = new ArrayList();
//        for(int i = 0; i < compute.prevActions.size(); i++)
//        {
//            actions.add(compute.prevActions.get(i));
//        }
//        for(int i = 0; i < compute.ea.actionSequence.size(); i++)
//        {
//            GMEAction act = thisController.getActionMap().get(compute.ea.actionSequence.get(i).actionName());
//            actions.add(act);
//        }
//        return thisController.getReward(actions);
//    }

    private StateNode findInTree(DynamicMDPState s)
    {
        for(int i = 0; i < tree.nodes.size(); i++)
        {
            if(tree.nodes.get(i).s.equals(s)) return tree.nodes.get(i);
        }
        return null;
    }

    void rehighlightPath(boolean changeCurrentState, boolean removeHiddenBan) 
    {   
        if(changeCurrentState)
        {
            DynamicMDPState oldCurrentState = lastComputeState.prevStates.get(lastComputeState.prevStates.size() - 1).s;
            DynamicMDPState newCurrentState = lastComputeState.thisState.s;
        
            nodeMap.get(oldCurrentState).set("CurrentState", false);
            vis.getVisualItem("graph.nodes", nodeMap.get(oldCurrentState)).setStroke(new BasicStroke(0));
        
            nodeMap.get(newCurrentState).set("CurrentState", true);
            vis.getVisualItem("graph.nodes", nodeMap.get(newCurrentState)).setStroke(new BasicStroke(10));
        }
        else
        {
            DynamicMDPState currentState = lastComputeState.thisState.s;
            nodeMap.get(currentState).set("CurrentState", true);
            vis.getVisualItem("graph.nodes", nodeMap.get(currentState)).setStroke(new BasicStroke(10));
        }
        
//        for(int i = 0; i < graph.getNodeCount(); i++)
//        {
//            
//            if(!graph.getNode(i).get("stateClass").equals(lastComputeState.thisState.s) && graph.getNode(i).getBoolean("CurrentState") && changeCurrentState)
//            {
//                graph.getNode(i).set("CurrentState", false);
//                vis.getVisualItem("graph.nodes", graph.getNode(i)).setStroke(new BasicStroke(0));
//            }
//            if(graph.getNode(i).get("stateClass").equals(lastComputeState.thisState.s))
//            {
//                graph.getNode(i).set("CurrentState", true);
//                vis.getVisualItem("graph.nodes", graph.getNode(i)).setStroke(new BasicStroke(10));
//            }
//        }
        
        //for each chosen state
        for(int i = 0; i < this.chosenStates.size() - 1; i++)
        {
            //find in the graph
            Node firstNode = nodeMap.get(this.chosenStates.get(i));
            Node secondNode = nodeMap.get(this.chosenStates.get(i+1));
//            for(int j = 0; j < graph.getNodeCount(); j++)
            {
                //ok we found the state in the graph
//                if(graph.getNode(j).get("stateClass").equals(this.chosenStates.get(i)))
                {
                    //now find the NEXT state in the graph
//                    for(int k = 0; k < graph.getNodeCount(); k++)
                    {
                        //now we have found both states in the graph
//                        if(graph.getNode(k).get("stateClass").equals(this.chosenStates.get(i + 1)))
                        {
                            //get the edge and set the weight accordingly
                            graph.getEdge(firstNode, secondNode).set("weight", 200);
                        }
                    }
                }
            }
        }
    }
    
    private void setStatePallette(boolean init)
    {
        if(pinkNode) 
        {
            s_pallette = Originals_pallette.clone();
        }
        else
        {
            s_pallette = new int[3];
            s_pallette[0] = Originals_pallette[0];
            s_pallette[1] = Originals_pallette[1];
            s_pallette[2] = Originals_pallette[2];
        }
        if(!init)
        {
            color.remove(fill);
            fill = new DataColorAction("graph.nodes", "nodeInfo", Constants.ORDINAL, VisualItem.FILLCOLOR, s_pallette);   
            fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255,255, 0));
            color.add(fill);
        }
    }
    
    /**
     * This function changes the color pallete for edges based on what type of actions are present.
     * @param init 
     */
    private void setEdgePallette(boolean init)
    {
        for(int i = 0; i < graph.getEdgeCount(); i++)
        {
            if(graph.getEdge(i).get("inPath").equals(1))
            {
                this.containsNonOptimalPath = true;
                break;
            }
        }
        
        
        if(this.containsNonOptimalPath && this.containsSubOptimalPath)
        {
            e_pallette = new int[3];
            e_pallette[0] = Originale_pallette[0];
            e_pallette[1] = Originale_pallette[1];
            e_pallette[2] = ColorLib.rgb(0, 191, 255);
        }
        else if(this.containsSubOptimalPath)
        {
            e_pallette = new int[2];
            e_pallette[0] = Originale_pallette[0];
            e_pallette[1] = ColorLib.rgb(0, 191, 255);
        }
        else if(this.containsNonOptimalPath)
        {
            e_pallette = Originale_pallette.clone();
        }
        else
        {
            e_pallette = new int[1];
            e_pallette[0] = Originale_pallette[0];
        }
        if(!init)
        {
            color.remove(dcaEdges);
            color.remove(arrowColor);
            dcaEdges = new DataColorAction("graph.edges", "inPath",  Constants.NOMINAL, VisualItem.STROKECOLOR, e_pallette);
            dcaEdges.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 255, 0));
            arrowColor = new DataColorAction("graph.edges", "inPath",Constants.NOMINAL, VisualItem.FILLCOLOR, e_pallette);
            arrowColor.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 255, 0));
            color.add(dcaEdges);
            color.add(arrowColor);
        }
    }
}


