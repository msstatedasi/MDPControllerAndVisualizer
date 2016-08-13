/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamicmdpcontroller;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.RewardFunction;
import dynamicmdpcontroller.actions.GMEAction;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author stefano
 */
public class Reward implements RewardFunction {

    private Termination termination = null;
    private double maxExecutionTime = -1;
    private double maxCost = -1;
    private double minExecutionTime = Double.MAX_VALUE;
    private double minCost = Double.MAX_VALUE;
    private double wt = 1, wc = 0;
    private HashMap<GMEAction, Double> reward = null;

    public Reward() {
        reward = new HashMap<>();
    }

    public void cacheActionRewards(List<GMEAction> actions) {

        for (GMEAction a : actions) {
            if (a.getExecTime() > maxExecutionTime) {
                maxExecutionTime = a.getExecTime();
            }
            if (a.getCost() > maxCost) {
                maxCost = a.getCost();
            }
            if (a.getExecTime() < minExecutionTime) {
                minExecutionTime = a.getExecTime();
            }
            if (a.getCost() < minCost) {
                minCost = a.getCost();
            }
        }
        for (GMEAction a : actions) {
            double rewardScore = rewardFunction(a);
            reward.put(a, rewardScore);
        }
    }

    private double rewardFunction(GMEAction a) 
    {
        double ret = -wt * a.getExecTime() - wc * a.getCost();
//                / (maxExecutionTime - minExecutionTime + 1 ) 
        return ret;
    }

    @Override
    public double reward(State state, Action action, State state1) {
//        if (termination.isTerminal(state)) {
//            return 100;
//        } else {
//        return -10;
//        }
        DynamicMDPState sPrime = (DynamicMDPState) state1;
        if (sPrime.isForbidden()) {
            return Double.NEGATIVE_INFINITY;
        }
        GMEAction a = (GMEAction) action;
        return reward.get(a);
    }

    public void setTermination(Termination t) {
        this.termination = t;
    }

    public double getWt() {
        return wt;
    }

    public void setWt(double wt) 
    {
        this.wt = wt;
    }

    public double getWc() {
        return wc;
    }

    public void setWc(double wc) {
        this.wc = wc;
    }

}
