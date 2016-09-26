package visual;

import java.awt.Color; 
import java.awt.BasicStroke; 
import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import org.jfree.chart.ChartPanel; 
import org.jfree.chart.JFreeChart; 
import org.jfree.data.xy.XYDataset; 
import org.jfree.data.xy.XYSeries; 
import org.jfree.ui.ApplicationFrame; 
import org.jfree.chart.plot.XYPlot; 
import org.jfree.chart.ChartFactory; 
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation; 
import org.jfree.data.xy.XYSeriesCollection; 
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;


/**
 * This class charts the various rewards of being in states and executing actions depending on the reward function specified.
 * @author jlewis
 */
public class Chart
{
      finalNumberFormat xFormat = new finalNumberFormat(true);
      finalNumberFormat yFormat = new finalNumberFormat(false);
      JFreeChart xylineChart;
      JFreeChart xylineChartAction;
      ChartPanel stateChartPanel;
      ChartPanel actionChartPanel;
      XYLineAndShapeRenderer actionRenderer;
      XYSeries chosenActions;
      XYSeries chosenCumulativeActions;
      XYSeriesCollection actionDataset;
      XYSeries chosenStates;
      XYSeriesCollection stateDataset;
      int chosenTimeStep = 1;
      StateValueContainer svc;
      StateValueContainer chosenSVC;
      ActionValueContainer chosenAVC;
      XYLineAndShapeRenderer renderer;
      JFrame frame;
      
      /**
       * Closes the frame tied to this instance.
       */
    public void close()
    {
        frame.dispose();
    }
    /**
     * This function sets up the charts according to how valueContainer and actionValueContiner are filled
     * @param valueContainer The container for the values of the state's values.
     * @param actionValueContainer A container of the action's taken and their rewards.
     */
   public Chart(StateValueContainer valueContainer, ActionValueContainer actionValueContainer)
    {
//        super("Comparisons of rewards");
        frame = new JFrame("Comparisons of rewards");
//        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        svc = valueContainer;
        xylineChart = ChartFactory.createXYLineChart(
             "" ,
            "Time Step" ,
            "State-Value" ,
             createStateDataset(valueContainer) ,
             PlotOrientation.VERTICAL ,
             true , true , false);
        stateChartPanel = createStateChartPanel(xylineChart, valueContainer);

      
        xylineChartAction = ChartFactory.createXYLineChart(
             "" ,
             "Time Step" ,
             "Reward" ,
             createActionDataset(actionValueContainer) ,
             PlotOrientation.VERTICAL ,
             true , true , false);
        actionChartPanel = createActionChartPanel(xylineChartAction, actionValueContainer);
      
      
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.add(stateChartPanel);
        p.add(actionChartPanel);
        frame.add(p);
        frame.pack( );                    
        frame.setVisible( true ); 
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        
        chosenActions = new XYSeries("Chosen Actions");
        chosenStates = new XYSeries("Chosen States");
        chosenCumulativeActions = new XYSeries("Chosen Cumulative Actions");
//        addStateValueAndAction(1);
    }
   
   public void update()
   {
        if(chosenTimeStep == 1) 
        {
            stateDataset.addSeries(chosenStates);
//            actionDataset.addSeries(chosenActions);
            actionDataset.addSeries(chosenCumulativeActions);
            
            chosenStates.add(0, svc.getStateValues().get(0));
            actionRenderer.setSeriesToolTipGenerator(1, new FinalToolTipChart(chosenAVC));
//            actionRenderer.setSeriesToolTipGenerator(3, new FinalToolTipChart(chosenAVC));
            renderer.setSeriesToolTipGenerator(1, new FinalToolTipChartState((chosenSVC)));
        }
//       chosenActions.add(chosenTimeStep - 1, chosenAVC.getRewards().get(chosenTimeStep - 1));
       chosenCumulativeActions.add(chosenTimeStep - 1, chosenAVC.getCumulativeReward().get(chosenTimeStep - 1));
       chosenStates.add(chosenTimeStep, chosenSVC.getStateValues().get(chosenTimeStep - 1));
       chosenTimeStep++;
   }
   
   public void addStateValueAndAction(double stateValue, double actionReward, String actionName)
   {
        if(chosenTimeStep == 1) 
        {
            stateDataset.addSeries(chosenStates);
            actionDataset.addSeries(chosenActions);
            
            chosenStates.add(0, svc.getStateValues().get(0));
            actionRenderer.setSeriesToolTipGenerator(1, new FinalToolTipChart(chosenAVC));
//            actionRenderer.setSeriesToolTipGenerator(3, new FinalToolTipChart(chosenAVC));
            renderer.setSeriesToolTipGenerator(1, new FinalToolTipChartState((chosenSVC)));
        }
        chosenActions.add(chosenTimeStep - 1, actionReward);
        chosenStates.add(chosenTimeStep, stateValue);
//        for(int i = 0; i < chosenAVC.)

//        update();
        chosenTimeStep++;
   }
   
   public void setChosenValueContainers(StateValueContainer chosenSVC, ActionValueContainer chosenAVC)
   {
       this.chosenAVC = chosenAVC;
       this.chosenSVC = chosenSVC;
   }
   
   /**
    * Creates the Chart panel for the actions taken.
    * @param xylineChartAction
    * @param actionValueContainer
    * @return 
    */
   private ChartPanel createActionChartPanel(JFreeChart xylineChartAction, ActionValueContainer actionValueContainer)
   {
      actionChartPanel = new ChartPanel(xylineChartAction);
      actionChartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
      actionChartPanel.setInitialDelay(0);
      final XYPlot actionPlot = xylineChartAction.getXYPlot( );
      actionRenderer = new XYLineAndShapeRenderer( );
      
      
      actionRenderer.setSeriesToolTipGenerator(0, new FinalToolTipChart(actionValueContainer));
//      actionRenderer.setSeriesToolTipGenerator(1, new FinalToolTipChart(actionValueContainer));

      

      actionRenderer.setSeriesPaint( 0 , Color.GREEN );
      actionRenderer.setSeriesPaint( 1 , Color.RED );
//      actionRenderer.setSeriesPaint(2, Color.RED);
//      actionRenderer.setSeriesPaint(3, Color.RED);
      actionRenderer.setSeriesStroke( 0 , new BasicStroke( 4.0f ) );
      actionRenderer.setSeriesStroke( 1 , new BasicStroke( 4.0f ) );
//      actionRenderer.setSeriesStroke(2, new BasicStroke(4.0f));
//      actionRenderer.setSeriesStroke(3, new BasicStroke(4.0f));
      actionRenderer.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator("{2}",xFormat,yFormat));
      actionRenderer.setBaseItemLabelsVisible(true);
      
        actionPlot.setRenderer( actionRenderer );
//        final NumberAxis xAxis = (NumberAxis) actionPlot.getDomainAxis();
//        actionPlot.setDomainAxis(xAxis);
//        ((NumberAxis)actionPlot.getDomainAxis()).setLowerBound(-.25);
     
     return actionChartPanel;
   }
   
   
   
   private ChartPanel createStateChartPanel(JFreeChart xylineChart, StateValueContainer svc)
   {
      stateChartPanel = new ChartPanel( xylineChart );
      
      stateChartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
      stateChartPanel.setInitialDelay(0);
      XYPlot plot = xylineChart.getXYPlot( );
      renderer = new XYLineAndShapeRenderer( );

      renderer.setSeriesToolTipGenerator(0, new FinalToolTipChartState(svc));
//      renderer.setSeriesToolTipGenerator(1, new FinalToolTipChartState((svc)));

      renderer.setSeriesPaint( 0 , Color.GREEN );
      renderer.setSeriesPaint( 1 , Color.RED );
      renderer.setSeriesStroke( 0 , new BasicStroke( 4.0f ) );
      renderer.setSeriesStroke( 1 , new BasicStroke( 4.0f ) );
      renderer.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator("{2}",xFormat,yFormat));
      renderer.setBaseItemLabelsVisible(true);
      
      plot.setRenderer( renderer );

//     final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
//     xAxis.setTickUnit(new NumberTickUnit(1));
//     plot.setDomainAxis(xAxis);
//     ((NumberAxis)plot.getDomainAxis()).setLowerBound(-.25);
     
     return stateChartPanel;
   }
   
   private XYDataset createStateDataset( StateValueContainer valueContainer )
   {
       XYSeries indivStates = new XYSeries( "Individual States" );
       //XYSeries cumulative = new XYSeries( "Cumulative" );
       for(int i = 0; i < valueContainer.getStateValues().size(); i++)
       {
            indivStates.add(i,valueContainer.getStateValues().get(i));
       }
       
      stateDataset = new XYSeriesCollection( );          
      stateDataset.addSeries(indivStates);
      return stateDataset;
   }
   
   private XYDataset createActionDataset( ActionValueContainer valueContainer)
   {
       XYSeries indivAction = new XYSeries( "Individual Actions" );
       XYSeries cumulative = new XYSeries("Cumulative");
       for(int i = 0; i < valueContainer.getRewards().size(); i++)
       {
           indivAction.add(i, valueContainer.getRewards().get(i));
       }
       for(int i = 0; i < valueContainer.getCumulativeReward().size(); i++)
       {
           cumulative.add(i, valueContainer.getCumulativeReward().get(i));
       }
       actionDataset = new XYSeriesCollection();
//       actionDataset.addSeries(indivAction);
       actionDataset.addSeries(cumulative);
       return actionDataset;
   }
}