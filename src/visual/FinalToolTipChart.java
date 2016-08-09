/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visual;

import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;

/**
 *
 * @author jlewis
 */
public class FinalToolTipChart implements XYToolTipGenerator {
    ActionValueContainer actValCont;

    public FinalToolTipChart(ActionValueContainer actionValueContainer) 
    {
        actValCont = actionValueContainer;
    }
    
    
    
    @Override
    public String generateToolTip(XYDataset xyd, int i, int i1) 
    {
        String s = Double.toString((double) Math.round(xyd.getYValue(i, i1)*100000) / 100000);
        String actName = actValCont.getActionName(i1);
        return actName + " (" + String.valueOf(i1) + ", " + s + ")";
    }
    
}
