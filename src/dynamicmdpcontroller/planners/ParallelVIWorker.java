/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamicmdpcontroller.planners;

import burlap.statehashing.HashableState;
import java.util.Date;
import java.util.Set;



/**
 *
 * @author stefano
 */
public class ParallelVIWorker implements Runnable {

    private Set<HashableState> states = null;
    private int index = 0;

    public void setIndex(int index) {
        this.index = index;
    }

    private ParallelVI parallelVI = null;

    public void setStates(Set<HashableState> states) {
        this.states = states;
    }


    public void setParallelVI(ParallelVI parallelVI) {
        this.parallelVI = parallelVI;
    }

    public void runVI() {

        double delta = 0.;
        int stateNum = 1;
        for (HashableState sh : states) {
            if (stateNum % 1000 == 0) {
//                System.out.println("Thread id: " + index + "\t State " + stateNum + "/" + states.size());
            }
            stateNum++;
            double v = parallelVI.value(sh);
           
//            double maxQ = parallelVI.performBellmanUpdateOn(sh);
            double maxQ = parallelVI.performBellmanUpdateOn(sh, index);
            delta = Math.max(Math.abs(maxQ - v), delta);
        }
        
        parallelVI.setDelta(delta, index);
        

//		DPrint.cl(this.debugCode, "Passes: " + i);
    }

    @Override
    public void run() {

//        Date d1 = new Date();
        runVI();
//        Date d2 = new Date();
//        System.out.println("Thread lifetime: " + (d2.getTime() - d1.getTime()));
    }

}
