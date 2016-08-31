/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BurlapVisualizer;

import dynamicmdpcontroller.controllers.FinalStateException;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;


//This is the class with the main function.  All it does is simply
// open a JFrame with text input boxes that allow you to enter
// your initial state.  After you push the ok button it calls the
// controller with the attributes you described.
/**
 * This is the main class where execution starts.
 * <p>
 * This is the class that handles what state space gets visualized based on user input.
 * 
 * @author jlewis
 */
public class TestProject {

    private static JFrame frame;


    private static JPanel rewardPanel;
    private static JPanel mainPanel;
    private static List<JLabel> rewardLabels;
    private static List<JTextField> textEnteredReward;
    private static JButton button;
    private static Manager m = null;

    /**
     * main method
     *
     * @param args[] the command line arguements
     */
    public static void main(String args[]) throws IOException 
    {
//        MyController temp = new MyController(0, 0, 0, 0, 0, 0, 0, "", 1, 0, 0, 0.9); //we make a temp controller to get the names of attributes

//        List<String> attrNames = temp.getAllStateAttributes();//get the name of every defined attribute
        setUpUI();
        
//        m = new Manager(); //then create new instance
//        m.run();
    }

    /**
     * Sets up the User Interface.
     * <p>
     * This method simply creates a JFrame and then adds as many input text
     * fields and labels to describe those text fields as neccesarry to describe
     * the initial state. Also adds an OK button at the bottom. From there it
     * calls Manager to set up the state space. When it detects that the OK
     * button has been pressed again it closes everything it has and starts
     * fresh.
     *
     * @param allAttribsForState this is a list of all the names of attributes
     * for states
     */
    public static void setUpUI() {

        button = new JButton("OK");
        button.addActionListener(new ButtonListener());
        button.setPreferredSize(new Dimension(40, 40));
        JPanel buttonPanel = new JPanel(new GridLayout(3, 3));
        buttonPanel.add(new JLabel(""));
        buttonPanel.add(new JLabel(""));
        buttonPanel.add(new JLabel(""));
        buttonPanel.add(new JLabel(""));
        buttonPanel.add(button);
        buttonPanel.add(new JLabel(""));
        buttonPanel.add(new JLabel(""));
        buttonPanel.add(new JLabel(""));
        buttonPanel.add(new JLabel(""));


        rewardLabels = new ArrayList<>();
        textEnteredReward = new ArrayList<>();
        

        

        rewardPanel = new JPanel();
        mainPanel = new JPanel();
        

        rewardPanel.setLayout(new GridLayout(0,2));
        
        
        rewardPanel.add(new JLabel("Reward parameters"));
        rewardPanel.add(new JLabel(" "));
        
        frame = new JFrame("Input for Burlap");


        
        rewardLabels.add(new JLabel("Cost"));
//        rewardLabels.add(new JLabel("Impact"));
        rewardLabels.add(new JLabel("Response Time"));
        rewardLabels.add(new JLabel("Gamma"));
        rewardLabels.add(new JLabel("Degredation"));
        for(int i = 0; i < 4; i++)
        {
            rewardPanel.add(rewardLabels.get(i));
            textEnteredReward.add(new JTextField());
            rewardPanel.add(textEnteredReward.get(i));
        }
        mainPanel.add(rewardPanel);
        mainPanel.add(buttonPanel);
        mainPanel.setLayout(new GridLayout(0, 2));
        frame.add(mainPanel);//add main panel
        frame.pack();//make frame as small as possible and still keep integrity

        frame.setVisible(true);

    }

    /**
     * This listens to the OK button and when pushed returns double value of each text field
     * 
     */
    private static class ButtonListener implements ActionListener {

        
        /**
         * 
         * @param e it is the text of the button which is OK.  If pressed return data. 
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("OK")) {
                
                if (m != null) { try {
                    //if there is a manager running close it.
                    m.close();
                    } catch (FinalStateException ex) {
                        Logger.getLogger(TestProject.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(TestProject.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ParseException ex) {
                        Logger.getLogger(TestProject.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                
                List<Double> values = getValues();
                try 
                {
                    System.out.println("cost: "+ values.get(0) + "time: "+ values.get(1) + "gamma: " + values.get(2) + "Degredation: " + values.get(3));
                    m = new Manager(); //then create new instance
                    m.run(values.get(0), values.get(1),
                            values.get(2), values.get(3)); //the value list is in correct order to do this
                    
                    //m.run will run until this method is called again(by user pushing OK button) causing this m.run to close and another open
                }
                catch (Exception ex)//this execption will only be caught if values.get(n) returns null(should never happen) 
                {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "The provided input is invalid.");
                }                
            }
        }

        /**
         * Returns a list of doubles to specify the initial state and the reward function.
         * <p>
         * index [0] - [6] are the initial state
         * index [7] - [9] are the reward function in the following format
         * [7] = cost
         * [8] = impact
         * [9] = time
         * [10] = gamma
         * [11] = allowedDegredation
         * @return List of <Doubles> that define the values the user specified for each attribute and reward function. 
         */
        public List<Double> getValues() {
            List<Double> value = new ArrayList<>();
            double sum = 0;

            if("".equals(textEnteredReward.get(0).getText()) && "".equals(textEnteredReward.get(1).getText())) //empty
            {
                value.add(1.0); //cost = 1
                value.add(0.0);// time = 0
                sum = 1;
            }
            else
            {
                for(int i = 0; i < 2; i++) //go through each reward value
                {
                    double rewardValue;
                    if(textEnteredReward.get(i).getText().equals("")) rewardValue = 0;
                    else rewardValue = Double.parseDouble(textEnteredReward.get(i).getText());
                    value.add(rewardValue);
                    sum += rewardValue;
                }
            }

            if(sum < 1 || sum > 1) return null;
            
            double gamma = 0;
            if(textEnteredReward.get(2).getText().equals(""))
            {
                gamma = 0;
            }
            else
            {
                gamma = Double.parseDouble(textEnteredReward.get(2).getText());
                if(gamma < 0 || gamma >=1) return null;
            }
            value.add(gamma);
            
            if("".equals(textEnteredReward.get(3).getText()))
            {
                value.add(-1.0);
            }
            else
            {
                value.add(Double.parseDouble(textEnteredReward.get(3).getText()));
            }

            
            
            return value;
        }
    }
}
