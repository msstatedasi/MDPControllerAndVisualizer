/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamicmdpcontroller.planners;

import burlap.mdp.core.action.Action;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import java.util.List;
import java.util.Set;

/**
 *
 * @author stefano
 */
public class StateGeneratorSetWorker implements Runnable {

    private ParallelVI parallelVI = null;
    private Set<HashableState> states = null;
        private int id = -1;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

        

    public StateGeneratorSetWorker(ParallelVI p) {
        this.parallelVI = p;
    }

    public void setState(Set<HashableState> sh) {
        this.states = sh;
    }

    @Override
    public void run() {
        for (HashableState sh : states) {
            if (sh == null) {
                return;
            }
            //skip this if it's already been expanded
            if (parallelVI.valueFunctionContainsKey(sh)) {
                return;
            }

            //do not need to expand from terminal states if set to prune
            if (parallelVI.getModel().terminal(sh.s()) && parallelVI.getStopReachabilityFromTerminalStates()) {
                return;
            }

            parallelVI.valueFunctionPut(sh);

            List<Action> actions = parallelVI.getApplicableActions(sh.s());
            for (Action a : actions) {
                List<TransitionProb> tps = ((FullModel) parallelVI.getModel()).transitions(sh.s(), a);
                for (TransitionProb tp : tps) {
                    HashableState tsh = parallelVI.stateHash(tp.eo.op);
                    if (!parallelVI.setContains(tsh) && !parallelVI.valueFunctionContainsKey(tsh)) {
                        parallelVI.addToSet(tsh);
                        parallelVI.addToList(tsh);
                    }
                }
            }
        }
    }

}
