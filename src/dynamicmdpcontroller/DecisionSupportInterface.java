/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamicmdpcontroller;

import burlap.behavior.singleagent.Episode;
import dynamicmdpcontroller.actions.GMEAction;
import dynamicmdpcontroller.controllers.FinalStateException;
import java.util.List;

/**
 *
 * @author stefano
 */
public interface DecisionSupportInterface {
    
    List<GMEAction> getLocalOptimalPathActions(int index, DynamicMDPState d) throws FinalStateException;
    List<GMEAction> getGlobalOptimalPathActions(DynamicMDPState d)throws FinalStateException, Exception;
    List<DynamicMDPState> getGlobalOptimalPath(DynamicMDPState s) throws FinalStateException;
    List<DynamicMDPState> getLocalOptimalPath(int index, DynamicMDPState s) throws FinalStateException;
    double getGlobalPathReward(List<DynamicMDPState> states, List<GMEAction> acts) throws FinalStateException;
    double getLocalPathReward(int index, DynamicMDPState s) throws FinalStateException;
    double getLocalPathReward(int index, List<GMEAction> actions, List<DynamicMDPState> states);
    Episode getEpisodeFromState(int index, DynamicMDPState s);
    Episode getGlobalEpisodeFromState(DynamicMDPState s);
    void printState(DynamicMDPState s);
    String stateString(DynamicMDPState s);
    List<String> getAllStateAttributes(DynamicMDPState s);
    Object getValueForAttribute(DynamicMDPState s, String str);
    List<GMEAction> getAllLocalDefinedActions(int index);
    List<GMEAction> getAllGlobalDefinedActions();
    
    double getLocalStateValue(int index, DynamicMDPState s) throws FinalStateException;
    double getGlobalStateValue(DynamicMDPState s) throws FinalStateException;
    
    DynamicMDPState getInitalState(int index);
    List<DynamicMDPState> getAllStates(int index);
    List<DynamicMDPState> getResultingStates(DynamicMDPState s, GMEAction a);
    boolean isTerminalState(int index, DynamicMDPState s);
    boolean isTerminalState(DynamicMDPState s); 
    
    
    int getNumOfLocalControllers();
    String getNameOfController(int index);
}
