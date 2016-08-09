/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visual;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import prefuse.visual.VisualItem;

/**
 * This class describes the buttons in {@link visual.DataDisplay} that point to edges in the state space.
 * @author jlewis
 */

public class ActionButton extends JButton {
    public int index; //just a way to hold index of this button in a list
    public DataDisplay dataDisplay;
    public String actionName;
    public VisualItem item;
    public FinalControlListener fcl;
    
    ActionButton(String text, int index, String actionName, FinalControlListener fcl, VisualItem item)
    {
        this.item = item;
        this.fcl = fcl;
        this.actionName = actionName;
        this.index = index;
        this.setText(text);
        this.addActionListener(new buttonListener());
    }
    
    private class buttonListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e) {
            fcl.itemClicked(item, null);
        }
    }
}
