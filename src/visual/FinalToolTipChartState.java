/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visual;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;

/**
 *
 * @author jlewis
 */
public class FinalToolTipChartState implements XYToolTipGenerator 
{
    StateValueContainer svc;

    public FinalToolTipChartState(StateValueContainer svc) {
        this.svc = svc;
    }
    

    @Override
    public String generateToolTip(XYDataset xyd, int i, int i1) {
        String s = Double.toString((double) Math.round(xyd.getYValue(i, i1)*100000) / 100000);
        String actName = svc.getActionNameFor(i1);
        return actName + " (" + String.valueOf(i1) + ", " + s + ")";
    }
    
}
