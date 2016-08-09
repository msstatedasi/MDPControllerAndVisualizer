/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visual;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jlewis
 */
public class StateValueContainer {
    private List<Double> StateValue;
    private List<Double> CumulativeValue;
    private ActionValueContainer avc;

    public StateValueContainer(ActionValueContainer avc) 
    {
        StateValue = new ArrayList<>();
        CumulativeValue = new ArrayList<>();
        this.avc = avc;
    }
    
    
    void addStateValue(double v)
    {
        StateValue.add(v);
    }
    List<Double> getStateValues()
    {
        return StateValue;
    }
    String getActionNameFor(int index)
    {
        if(index == 0) return "Initial State";
        else
        {
            return avc.getActionName(index - 1);
        }
    }
    List<Double> getCumlativeValues()
    {
        double sum = 0;
        for(int i = 0; i < StateValue.size(); i++)
        {
            sum += StateValue.get(i);
            try
            {
                CumulativeValue.set(i, sum); 
            }
            catch(Exception e)
            {
                CumulativeValue.add(sum);
            }
        }
        return CumulativeValue;
    }
}
