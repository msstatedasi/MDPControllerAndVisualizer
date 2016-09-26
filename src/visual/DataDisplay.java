/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visual;

import Tree.StateTree;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import prefuse.visual.VisualItem;
import BurlapVisualizer.MyController;
import dynamicmdpcontroller.DynamicMDPState;
import dynamicmdpcontroller.actions.GMEAction;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
//import org.apache.commons.lang3.StringUtils;


/**
 * This class contains all the info about the visualizer in another window.
 * @author jlewis
 */
public class DataDisplay implements ItemListener
{
    
    private StateTree tree;
    private List<String> allAttribs;
    
    private String HTMLHeader;
    private String HTMLTail;
    private String seperator;
    
    
    JFrame frame;
    JPanel p;
    JPanel list;
    JLabel srcTable;
    JLabel actionTable;
    JLabel resultTable;
    JLabel cost;
    JLabel impact;
    JLabel time;
    JLabel rewardTotal;
    JPanel display = null;
    JScrollPane scroll = null;
    JCheckBox ignoreDegCheckBox;
    boolean ignoreDegredation = false;
    
    MyController myController;
    FinalControlListener fcl;
    
    String prefix;
    
    JTree stateTree;
    JTree actionTree;
    JTree resultStateTree;
    HighlightTreeCellRenderer inithtcr;
    HighlightTreeCellRenderer actionhtcr;
    HighlightTreeCellRenderer resulthtcr;
    
    HashMap<String, DefaultMutableTreeNode> initNodeMap;
    HashMap<String, DefaultMutableTreeNode> actionNodeMap;
    HashMap<String, DefaultMutableTreeNode> resultStateNodeMap;
    
    int index;
    
    /**
     * When called this frame is disposed of.
     */
    public void close()
    {
        frame.dispose();
    }
    
    public boolean shouldIgnoreDegredation()
    {
        return ignoreDegredation;
    }
    
    /**
     * This set all initially needed data and opens a blank window.
     * @param t The state tree for the state space
     * @param allStateAttribs all defined attributes of a state.
     * @param controller The controller for this state space
     * @param takenActionItems the actions(in item form) of the actions taken
     * @param fcl The mouse control listener(used for when user clicks a button)
     */
    public DataDisplay(List<String> allStateAttribs, MyController controller)
    {
        this.index = index;
        ignoreDegCheckBox = new JCheckBox("Ignore degredation");
        ignoreDegCheckBox.addItemListener(this);
        
        myController = controller;
        initNodeMap = new HashMap<>();
        actionNodeMap = new HashMap<>();
        resultStateNodeMap = new HashMap<>();
        stateTree = new JTree();
        actionTree = new JTree();
        resultStateTree = new JTree();
        
        List<String> allDefinedActionsEver = new ArrayList();
        for(int i = 0; i < myController.getNumOfLocalControllers(); i++)
        {
            for(int j = 0; j < myController.allDefinedActions(i).size(); j++)
            {
                allDefinedActionsEver.add(myController.allDefinedActions(i).get(j));
            }
        }
        
        this.fcl = fcl;
        
        allAttribs = allStateAttribs;
        
        cost = new JLabel();
        impact = new JLabel();
        time = new JLabel();
        rewardTotal = new JLabel();
        
        
        JFrame treeFrame = new JFrame("State and Action info");
        treeFrame.setVisible(true);
        
        inithtcr = new HighlightTreeCellRenderer(stateTree.getCellRenderer());
        JScrollPane stateTreeView = createTree(allStateAttribs, initNodeMap, stateTree, inithtcr, false);
        
        actionhtcr = new HighlightTreeCellRenderer(actionTree.getCellRenderer());
        JScrollPane actionTreeView = createTree(allDefinedActionsEver, actionNodeMap, actionTree, actionhtcr, true);
        
        resulthtcr = new HighlightTreeCellRenderer(resultStateTree.getCellRenderer());
        JScrollPane resultStateTreeView = createTree(allStateAttribs, resultStateNodeMap, resultStateTree, resulthtcr, false);
        
        
        JPanel jpanel = new JPanel();
//        jpanel.setLayout(new GridLayout(1, 0));
//        jpanel.add(stateTreeView);
//        jpanel.add(actionTreeView);
//        jpanel.add(resultStateTreeView);
//        jpanel.setPreferredSize(new Dimension(700, 500));
        jpanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipadx = 100;
        c.gridx = 0;
        c.gridy = 0;
        jpanel.add(stateTreeView, c);
        
        c.gridx = 1;
        c.gridy = 0;

        jpanel.add(actionTreeView, c);
        
        c.gridx = 2;
        c.gridy = 0;
        jpanel.add(resultStateTreeView, c);
        
        c.gridx = 0;
        c.gridy = 1;
        jpanel.add(this.ignoreDegCheckBox, c);
                
        treeFrame.add(jpanel);
        treeFrame.pack();
        treeFrame.setVisible(true);
        treeFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        
        

        

        this.setUpCharts("no action", null, null);
    }
    
    public void giveNeededInfo(StateTree t, FinalControlListener fcl, int index)
    {
        tree = t;
        this.fcl = fcl;
        this.index = index;
        
//        double[] rewards = controller.computeTotalReward(t.actionsTaken);
//        double reward;
//        for(int i = 0; i < rewards.length; i++)
//        {
//            rewards[i] = (double) Math.round(rewards[i] * 100000) / 100000;
//        }
//        reward = controller.getReward(index, t.actionsTaken, t.statesTaken);
//        reward = (double) Math.round(reward * 100000) / 100000;
//        
//        cost.setText("Cost: " + String.valueOf(rewards[0]));
//        time.setText("Time: " + String.valueOf(rewards[1]));
//        rewardTotal.setText("Reward: " + String.valueOf(reward));
//        
    }
    
    public void updateTree(List<String> list, List<Object> values, HashMap<String, DefaultMutableTreeNode> map, JTree tree,
            List<String> toHighlight, HighlightTreeCellRenderer htcrUpdate)
    {
        htcrUpdate.q = new ArrayList();
        TreePath path = null;
        for(int i = 0; i < values.size(); i++)
        {
            if(values.get(i) == null) continue;
            String fullName = "";
            DefaultMutableTreeNode node = map.get(list.get(i));
            DefaultMutableTreeNode temp = node;
            while(temp.getUserObject() != "root")
            {
                if(fullName == "") fullName = (String) temp.getUserObject();
                else fullName = temp.getUserObject() + "_" + fullName;
                temp = (DefaultMutableTreeNode) temp.getParent();
            }
            String orginalName = (String) node.getUserObject().toString().split(" = ")[0];
            String modifiedName = orginalName + " = " + values.get(i).toString();
            node.setUserObject(modifiedName);
            path = new TreePath(node.getPath());
            path = path.getParentPath();
            tree.expandPath(path);
            if(toHighlight != null && toHighlight.contains(fullName))
            {
                htcrUpdate.q.add(fullName);
                
            }
        }
        tree.updateUI();

    }
    
    public void updateTree(String clickedAction, HighlightTreeCellRenderer htcrUpdate)
    {
        DefaultMutableTreeNode node = actionNodeMap.get(clickedAction);
        TreePath path = new TreePath(node.getPath());
        path = path.getParentPath();
        
        
        actionTree.expandPath(path);
        htcrUpdate.q.clear();
        htcrUpdate.q.add(clickedAction);
        actionTree.updateUI();
    }
    
    public void clearTree(List<String> list, HashMap<String, DefaultMutableTreeNode> map, JTree tree, HighlightTreeCellRenderer workingHTCR)
    {
        for(int i = 0; i < list.size(); i++)
        {
            DefaultMutableTreeNode node = map.get(list.get(i));
            node.setUserObject(node.getUserObject().toString().split(" = ")[0]);
        }
        workingHTCR.q = new ArrayList<>();
        tree.updateUI();
    }
    
    public JScrollPane createTree(List<String> list, HashMap<String, DefaultMutableTreeNode> map, JTree tree, HighlightTreeCellRenderer workingHTCR, boolean addSelectionListener)
    {
        
        String rootName = "root";
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(rootName);
        map.put(rootName, top);
        for(int i = 0; i < list.size(); i++)
        {
            DefaultMutableTreeNode prevFound = top;
            
            for(int j = 0; j < list.get(i).split("_").length; j++) //this was a last minute fix for a modeling issue
            {
                boolean found = false;
                String attrib = list.get(i).split("_")[j];
                
                Enumeration enu = prevFound.postorderEnumeration();
                while(enu.hasMoreElements())
                {
                    DefaultMutableTreeNode find = (DefaultMutableTreeNode) enu.nextElement();
                    if(find.toString().equals(attrib))
                    {
                        prevFound = find;
                        found = true;
                        break;
                    }
                }
                if(!found)
                {
                    DefaultMutableTreeNode toAdd = new DefaultMutableTreeNode(attrib);
                    prevFound.add(toAdd);
                    map.put(list.get(i), toAdd); //the key is the WHOLE path not just the attrib name.
                    prevFound = toAdd;
                }
            }
        }
        
 
        //Create a tree that allows one selection at a time.
        DefaultTreeModel model = new DefaultTreeModel(top);

        
        tree.setModel(model);

//        tree = new JTree(top);
        
       
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellRenderer(workingHTCR);
        tree.updateUI();
//        jtree.add(top);
        JScrollPane treeView = new JScrollPane(tree);
        workingHTCR.equalSignCheck = true;
        if(addSelectionListener)
        {
            tree.addTreeSelectionListener(new MyTreeSelectionListener());
            workingHTCR.equalSignCheck = false;
        }
       
//        treeView.setPreferredSize(new Dimension(600, 600));
        return treeView;
    }
    

    
    /**
     * This function updates tables based on what has been clicked(or not clicked)
     * @param clickedAction String name of the clicked action("no action" if an action was not clicked)
     * @param srcState The source state that was clicked(or refered to by the action)
     * @param resultState The result state of the action on the src state
     */
    public void setUpCharts(String clickedAction, DynamicMDPState srcState, DynamicMDPState resultState)
    {
        HTMLHeader = "<html>";
        HTMLTail = "</html>";
        seperator = "<br>";
//        System.out.println("working on /it");
        clearTree(allAttribs, initNodeMap, stateTree, this.inithtcr);
        clearTree(allAttribs, resultStateNodeMap, resultStateTree, this.actionhtcr);
        clearTree(myController.allDefinedActions(index), actionNodeMap, actionTree, this.resulthtcr);
        List<Object> values = new ArrayList<>();
        if(srcState != null)
        {
            for(int i = 0; i < allAttribs.size(); i++)
            {
//                System.out.println(allAttribs.get(i).split("_")[3]);
                Object value = myController.getValueForAttribute(srcState, allAttribs.get(i));
                values.add(value);
            }
            List<String> difference = this.compareAttributes(srcState, resultState);
            updateTree(allAttribs, values, initNodeMap, stateTree, difference, inithtcr);
        }
        values.clear();
        if(resultState != null)
        {
            for(int i = 0; i < allAttribs.size(); i++)
            {
                Object value = myController.getValueForAttribute(resultState, allAttribs.get(i));
                values.add(value);
            }
            List<String> difference = this.compareAttributes(srcState, resultState);
            updateTree(allAttribs, values, resultStateNodeMap, resultStateTree, difference, resulthtcr);
        }
        if(!clickedAction.equals("no action")) updateTree(clickedAction, actionhtcr);

        
//        String wholeMessage = "";
//        wholeMessage += HTMLHeader;
//        wholeMessage += "<head>";
//        wholeMessage += "</head>";
//        wholeMessage += "<body>";
//        wholeMessage += setUpActionChart(clickedAction);
//        wholeMessage += seperator + seperator + seperator;
//        wholeMessage += "</body>";
//        wholeMessage += HTMLTail;       
//        actionTable = new JLabel(wholeMessage);
//        
//        List<String> differencesInStates = this.compareAttributes(srcState, resultState); 
//        
//        wholeMessage = "";
//        wholeMessage += HTMLHeader;
//        wholeMessage += "<head>";
//        wholeMessage += "</head>";
//        wholeMessage += "<body>";
//        wholeMessage += setUpStateChart(srcState, differencesInStates);
//        wholeMessage += seperator + seperator + seperator;
//        wholeMessage += "</body>";
//        wholeMessage += HTMLTail;         
//        srcTable = new JLabel(wholeMessage);
//        
//        wholeMessage = "";
//        wholeMessage += HTMLHeader;
//        wholeMessage += "<head>";
//        wholeMessage += "</head>";
//        wholeMessage += "<body>";
//        wholeMessage += setUpStateChart(resultState, differencesInStates);
//        wholeMessage += seperator + seperator + seperator;
//        wholeMessage += "</body>";
//        wholeMessage += HTMLTail;       
//        resultTable = new JLabel(wholeMessage);
        
//        if(frame == null) frame = new JFrame("Visualizer Information");
//        else frame.remove(p);
//        
//        p = new JPanel();
//        
//        list = new JPanel(); 
//        list.add(srcTable);
//        list.add(actionTable);
//        list.add(resultTable);

        // create the middle panel components
//        List<ActionButton> actionButtons = getButtonList();
//        if(display == null)
//        {
//            display = new JPanel();
//            for(int i = 0; i < actionButtons.size(); i++)
//            {
//                display.add(actionButtons.get(i));
//            }
//                scroll = new JScrollPane(display, 
//                JScrollPane.VERTICAL_SCROLLBAR_NEVER, 
//                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//                scroll.getHorizontalScrollBar().setUnitIncrement(8);
//                scroll.setPreferredSize(new Dimension(500, 50));
//        }

//        JPanel actionInfo = new JPanel();
////        actionInfo.add(cost);
////        actionInfo.add(impact);
////        actionInfo.add(time);
////        actionInfo.add(rewardTotal);
//        actionInfo.add(ignoreDegCheckBox);
//        
//
//       p.add(list);
////       p.add(scroll);
//       p.add(actionInfo);
//
////       frame.add(p);
//       frame.pack();
//       frame.setSize(1300, 700);
//       frame.setVisible(true);
    }
    
    
    
    /**
     * Sets up the table for state(in one table update this function will get called twice sometimes)
     * @param clickedState The state to make a table out of
     * @param different a string of the state attribute name that is different from the other state that was clicked
     * @return 
     */
    private String setUpStateChart(DynamicMDPState clickedState, List<String> different)
    {
        String wholeMessage = "";

        wholeMessage += "<table border =\"1\">";
        //wholeMessage += "<table border =\"1\" style =\"width:100%\">";
        wholeMessage += "<th>Attribute Name</th>";
        wholeMessage += "<th>Attribute Value</th>";
        
        for(int i = 0; i< allAttribs.size(); i++)
        {
            wholeMessage += "<tr>";
            String stateAttrib = allAttribs.get(i);
            
            if(clickedState != null)
            {
                boolean highlight = false; //flag to determine if state attribute should be highlighted
                if(different != null)
                {
                    for(int u = 0; u < different.size(); u++)
                    {
                        if(allAttribs.get(i).equals(different.get(u))) //the different string name and the current attribute exists so between s1 and s2 this attr is different
                        {
                            highlight = true;//so highlight it
                        }
                    }
                }
                
                if(highlight)
                {
                    wholeMessage += "<td><FONT style=\"BACKGROUND-COLOR: yellow\">" + stateAttrib + "</FONT></td>";
                    wholeMessage += "<td><FONT style=\"BACKGROUND-COLOR: yellow\">" + myController.getValueForAttribute(clickedState, stateAttrib) + "</td>";
                }
                else
                {
                    wholeMessage += "<td>" + stateAttrib + "</td>";
                    wholeMessage += "<td>" + myController.getValueForAttribute(clickedState, stateAttrib) + "</td>";
                }

            }
            else
            {
                wholeMessage += "<td>" + stateAttrib + "</td>";
                wholeMessage += "<td></td>";
            }
            wholeMessage += "</tr>";
        }
        wholeMessage += "</table>";

        return wholeMessage;
    }
    
    
    /**
     * This function sets the html for the action table
     * @param clickedAction the action name
     * @return 
     */
    private String setUpActionChart(String clickedAction)
    {

        String wholeMessage = "";

        wholeMessage += "<table border=\"1\" style=\"width:100%\">";
        wholeMessage += "<th>Action Name</th>";
        wholeMessage += "<th>Cost</th>";
        wholeMessage += "<th>Imact</th>";
        for(int i = 0; i < tree.allPossibleActions.size(); i++)
        {
            GMEAction act = tree.allPossibleActions.get(i);
            wholeMessage += "<tr>";
            if(clickedAction.equals(act.getName()))
            {
               wholeMessage += "<td><FONT style=\"BACKGROUND-COLOR: yellow\">" + act.getName() + "</FONT></td>"; 
               wholeMessage += "<td><FONT style=\"BACKGROUND-COLOR: yellow\">" + act.getCost() + "</FONT></td>";
//               wholeMessage += "<td><FONT style=\"BACKGROUND-COLOR: yellow\">" + act.getImpact() + "</FONT></td>";
               wholeMessage += "<td><FONT style=\"BACKGROUND-COLOR: yellow\">" + act.getExecTime() + "</FONT></td>";
            }
            else
            {
                wholeMessage += "<td>" + act.getName() + "</td>";
                wholeMessage += "<td>" + act.getCost() + "</td>";
//                wholeMessage += "<td>" + act.getImpact() + "</td>";
                wholeMessage += "<td>" + act.getExecTime() + "</td>";
            }

            wholeMessage += "</tr>";
            
            
        }
        
        wholeMessage += "</table>";

        return wholeMessage;
    }
    
    
    //returns a list of string with the names of attributes that are different between each state
    //it checks every every state value and appends the name of the attribute to differingValues which
    //is return so we know the names of the different values between s1 and s2
    private List<String> compareAttributes(DynamicMDPState s1, DynamicMDPState s2)
    {
        if(s2 == null) return null; //s1 will always be srcState but targetState may be null
        List<String> attrNames = myController.getAllStateAttributes(index);
        List<String> differingValues = new ArrayList<>();
//        List<Value> attrValue1 = myController.getAllStateValuesFor(s1);
//        List<Value> attrValue2 = myController.getAllStateValuesFor(s2);
        
        for(int i = 0; i < attrNames.size(); i++)
        {
            Object value1 = myController.getValueForAttribute(s1, attrNames.get(i));
            Object value2 = myController.getValueForAttribute(s2, attrNames.get(i));
            if(!value1.equals(value2)) //DOES NOT equal(different)
            {
                differingValues.add(attrNames.get(i));
            }
        }
        return differingValues;
    }

    @Override
    public void itemStateChanged(ItemEvent e) 
    {
        ignoreDegredation = true;
        ignoreDegCheckBox.setEnabled(false);
    }
    
    
    class MyTreeSelectionListener implements TreeSelectionListener
    {
        JFrame frame = null;
        JPanel panel = null;
        JTable table = null;
        JScrollPane scroll = null;
        @Override
        public void valueChanged(TreeSelectionEvent e) 
        {
           DefaultMutableTreeNode node = (DefaultMutableTreeNode) actionTree.getLastSelectedPathComponent();
           
           if(node == null) return;
           String actionName = "";
            TreeNode[] wholePath = node.getPath();
            for(TreeNode n : wholePath)
            {
                DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) n;
                actionName = actionName + "_" + dmtn.getUserObject();
            }
            actionName = actionName.substring(6);
            int numOfActionMaps = myController.getNumOfLocalControllers();
//           String actionName = node.getUserObject().toString().split(" = ")[0];
            int controllerNum = 0;
           GMEAction a = myController.getActionMap(index).get(actionName);
           while(a == null && controllerNum < numOfActionMaps)
           {
               a = myController.getActionMap(controllerNum).get(actionName);
               controllerNum++;
           }
           
           if(a == null) return;
           
           List<String> attributeNames = a.getActionParameterNames();
           Object[] attributeNamesArr = new Object[attributeNames.size()];
           Object[][] values = new Object[1][attributeNames.size()];
           for(int i = 0; i < attributeNames.size(); i++)
           {
               attributeNamesArr[i] = attributeNames.get(i);
               values[0][i] = a.getActionParameterValues(attributeNames.get(i));
           }
           
           if(frame == null)
           {
                frame = new JFrame();
           }
           
            table = new JTable(values, attributeNamesArr);
            table.setEnabled(false);
            scroll = new JScrollPane(table);
            panel = new JPanel();
            panel.add(scroll);
           
           frame.add(panel);
           frame.pack();
           frame.setVisible(true);
           frame.setTitle(a.actionName());
           
           
//           String[] paramters = myController.
        }
        
    }
    class HighlightTreeCellRenderer extends DefaultTreeCellRenderer 
    {
        private final Color rollOverRowColor = new Color(220, 240, 255);
        private final TreeCellRenderer renderer;
        public List<String> q;
        boolean equalSignCheck;
        public HighlightTreeCellRenderer(TreeCellRenderer renderer) 
        {
            q = new ArrayList<>();
            this.renderer = renderer;
        }
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean isSelected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) 
            {
                JComponent c = (JComponent)renderer.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);
                if(isSelected) 
                {
                    c.setOpaque(false);
                    c.setForeground(getTextSelectionColor());
                    c.setBackground(Color.BLUE); //getBackgroundSelectionColor());
                }
                else
                {
                    if(!q.isEmpty())
                    {
                        c.setOpaque(true);
                    }
                    boolean shouldHighlight = false;
                    for(int i = 0; i < q.size(); i++)
                    {
                        String valueStr = value.toString();
                        if(valueStr.split(" = ").length == 2 && q.get(i).contains(valueStr.split(" = ")[0]))//if(valueStr.contains(q.get(i)))//if(StringUtils.containsIgnoreCase(valueStr, q.get(i)))
                        {
                            shouldHighlight = true;
                            break;
                        }
                        else if(q.get(i).split("_")[3].contains(valueStr) && !equalSignCheck)
                        {
                            shouldHighlight = true;
                            break;
                        }
                    }
                    if(!shouldHighlight)
                    {
                        c.setForeground(getTextNonSelectionColor());
                        c.setBackground(getBackgroundNonSelectionColor());
                    }
                    else
                    {
                        c.setForeground(getTextNonSelectionColor());
                        c.setBackground(rollOverRowColor);
                    }
                }
            return c;
            }
        }
}
