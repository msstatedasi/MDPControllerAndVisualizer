/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visual;

import BurlapVisualizer.MyController;
import dynamicmdpcontroller.DecisionSupportConnection;
import dynamicmdpcontroller.DynamicMDPState;
import dynamicmdpcontroller.actions.GMEAction;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jlewis
 */
public class ActionValueContainer {
    private List<Double> rewardForAction;
    private List<Double> CumulativeValue;
    private List<String> actionName;
    private MyController controller;
    
    public ActionValueContainer(MyController c)
    {
        rewardForAction = new ArrayList<>();
        CumulativeValue = new ArrayList<>();
        actionName = new ArrayList<>();
        controller = c;
    }
    
    public void addAction(DynamicMDPState s, DynamicMDPState result, GMEAction act)
    {
        double value = controller.getReward(act, s);
        rewardForAction.add(value);
        actionName.add(act.getName());
    }
    public List<Double> getRewards()
    {
        return rewardForAction;
    }
    public List<Double> getCumulativeReward()
    {
        double sum = 0;
        for(int i = 0; i < rewardForAction.size(); i++)
        {
            sum += rewardForAction.get(i);
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
    public String getActionName(int i)
    {
        return actionName.get(i);
    }
}
