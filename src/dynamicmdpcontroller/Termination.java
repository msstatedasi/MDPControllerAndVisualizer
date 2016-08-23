/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamicmdpcontroller;

import burlap.mdp.core.state.State;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.el.ELProcessor;

/**
 *
 * @author stefano
 */
public class Termination implements burlap.mdp.core.TerminalFunction, Serializable {

    private String terminationCondition;
    private List<String> terminationAttributes = null;
    private String terminationName = null;
    
    public Termination() {
        terminationAttributes = new ArrayList<>();
        terminationName = new String();
    }

    @Override
    public boolean isTerminal(State state) {
        DynamicMDPState s = (DynamicMDPState) state;
        List<Object> attributeKeys = s.variableKeys();
        ELProcessor elprocessor = new ELProcessor();
        
        //Caching useful attributes
        if (terminationAttributes.isEmpty()) {
            for (Object o: attributeKeys) {
                String attributeKey = (String)o;
                if (terminationCondition.contains(attributeKey)) {
                    terminationAttributes.add(attributeKey);
                }
            }
        }
        
        for (String attributeKey : terminationAttributes) {
//            String attributeKey = (String) o;
            elprocessor.setValue(attributeKey, state.get(attributeKey));
        }
//        System.out.println(terminationCondition);
        Object eval = elprocessor.eval(terminationCondition);
        return Boolean.valueOf(eval.toString());
        
//        return true;
    }

    public String getTerminationCondition() {
        return terminationCondition;
    }

    public void setTerminationCondition(String terminationCondition) {
        this.terminationCondition = terminationCondition;
    }
    
    public void serialize(String path) throws Exception {
        FileOutputStream fos = new FileOutputStream("C:/Classes/termination.term");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
        oos.close();
        fos.close();
    }

    public String getTerminationName() {
        return terminationName;
    }

    public void setTerminationName(String terminationName) {
        this.terminationName = terminationName;
    }

    public List<String> getTerminationAttributes() {
        return terminationAttributes;
    }
    
    
    

}
