/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visual;

import prefuse.action.assignment.DataColorAction;

/**
 *
 * @author jlewis
 */
public class NewColorAction extends DataColorAction {
    
    public NewColorAction(String string, String string1, int i, String string2, int[] ints) {
        super(string, string1, i, string2, ints);
    }
    public void setFillColor(int color)
    {
        this.m_defaultColor = color;
        this.setDefaultColor(color);
        this.setFillColor(color);
    }
    
}
