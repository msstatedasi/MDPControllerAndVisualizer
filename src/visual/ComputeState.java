/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visual;

import Tree.StateNode;
import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import dynamicmdpcontroller.DynamicMDPState;
import dynamicmdpcontroller.actions.GMEAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author jlewis
 */
public class ComputeState 
{
    StateNode thisState;
    List<StateNode> prevStates;
    List<GMEAction> prevActions;
    Episode ea;
    boolean undoStates;
    GMEAction undoAction;
    boolean validEa = true;
    
    public ComputeState()
    {
        prevActions = new ArrayList();
        prevStates = new ArrayList<>();
        ea = new Episode();
//        undoStates = new ArrayList();
    }
    
    public List<DynamicMDPState> convertToStateList()
    {
        List<DynamicMDPState> s = new ArrayList();
        for(int i = 0; i < prevStates.size(); i++)
        {
            s.add(prevStates.get(i).s);
        }
        s.add(thisState.s);
        if(ea == null) return s;
        for(int i = 0; i < ea.stateSequence.size(); i++)
        {
            s.add((DynamicMDPState) ea.stateSequence.get(i));
        }
        return s;
    }
    
    public List<DynamicMDPState> getEffectiveStateList()
    {
        List<DynamicMDPState> effectiveList = new ArrayList();
        for(StateNode sn: this.prevStates)
        {
            effectiveList.add(sn.s);
        }
        effectiveList.add(thisState.s);
        
        if(!validEa) return effectiveList;
        
        for (State s : ea.stateSequence) 
        {
            DynamicMDPState ds = (DynamicMDPState) s;
            effectiveList.add(ds);
        }
        return effectiveList;
    }
    public List<GMEAction> getEffectiveActionList()
    {
        List<GMEAction> effectiveList = new ArrayList();
        for(GMEAction act : this.prevActions)
        {
            effectiveList.add(act);
        }
        
        if(!validEa) return effectiveList;
        
        for(Action a : ea.actionSequence)
        {
            GMEAction act = (GMEAction) a;
            effectiveList.add(act);
        }
        return effectiveList;
    }
    
    public boolean checkForDuplicates()
    {
        List<DynamicMDPState> list = this.convertToStateList();
        for(int i = 0; i < list.size(); i++)
        {
            for(int j = 0; j < list.size(); j++)
            {
                if(j != i && list.get(i).equals(list.get(j)))
                {
                    return true;
                }
            }
        }
        return false;
    }
    public void addStates(List<StateNode> prevStates, StateNode thisState, List<DynamicMDPState> postStates)
    {
        for(StateNode sn: prevStates)
        {
            this.prevStates.add(sn);
        }
        this.thisState = thisState;
        for(DynamicMDPState ds: postStates)
        {
            this.ea.stateSequence.add(ds);
        }
    }
}
