package dynamicmdpcontroller.planners;

import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.policy.SolverDerivedPolicy;
import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.MDPSolverInterface;
import burlap.behavior.valuefunction.QProvider;
import burlap.behavior.valuefunction.QValue;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.SampleModel;
import dynamicmdpcontroller.Termination;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.management.RuntimeErrorException;

/**
 * A greedy policy that breaks ties by randomly choosing an action amongst the
 * tied actions. This class requires a QComputablePlanner
 *
 * @author James MacGlashan
 *
 */
public class MyGreedyQPolicy extends GreedyQPolicy implements SolverDerivedPolicy, EnumerablePolicy {

    protected QProvider qplanner;
    protected Random rand;

    public MyGreedyQPolicy() {
        qplanner = null;
        rand = RandomFactory.getMapped(0);
    }

    /**
     * Initializes with a QComputablePlanner
     *
     * @param planner the QComputablePlanner to use
     */
    public MyGreedyQPolicy(QProvider planner) {
        qplanner = planner;
        rand = RandomFactory.getMapped(0);
    }

    @Override
    public void setSolver(MDPSolverInterface solver) {

        if (!(solver instanceof QProvider)) {
            throw new RuntimeErrorException(new Error("Planner is not a QComputablePlanner"));
        }

        this.qplanner = (QProvider) solver;
    }

    @Override
    public Action action(State s) {
//        System.out.println("MyGreedyQPolicy1");
        List<QValue> qValues = this.qplanner.qValues(s);
//        Iterator<QValue> qValuesIte = this.qplanner.qValues(s).iterator();
//        while (qValuesIte.hasNext()) {
//            QValue qValue = qValuesIte.next();
//            System.out.println("Value for action: " + qValue.a.actionName() + " = " + qValue.q);
//        }
//        System.out.println("Value for action: " + qValues.get(0).a.actionName() + qValues.get(0).q);
        List<QValue> maxActions = new ArrayList<QValue>();
        maxActions.add(qValues.get(0));
        double maxQ = qValues.get(0).q;
        for (int i = 1; i < qValues.size(); i++) {
//            System.out.println("Value for action: " + qValues.get(i).a.actionName() + qValues.get(i).q);
            QValue q = qValues.get(i);
            if (q.q > maxQ) {
                maxActions.clear();
                maxActions.add(q);
                maxQ = q.q;
            } else if (q.q == maxQ) {
                maxActions.add(q);
            }
        }

        int selected = rand.nextInt(maxActions.size());
        Action srcA = maxActions.get(selected).a;
//        System.out.println("Selected action=" + srcA.actionName() + ", q=" + maxQ);
        return srcA;
    }

    @Override
    public double actionProb(State s, Action a) {
        return PolicyUtils.actionProbFromEnum(this, s, a);
    }

    @Override
    public List<ActionProb> policyDistribution(State s) {
        System.out.println("MyGreedyQPolicy2");
        List<QValue> qValues = this.qplanner.qValues(s);
        int numMax = 1;
        double maxQ = qValues.get(0).q;
        for (int i = 1; i < qValues.size(); i++) {
            QValue q = qValues.get(i);
            if (q.q == maxQ) {
                numMax++;
            } else if (q.q > maxQ) {
                numMax = 1;
                maxQ = q.q;
            }
        }

        List<ActionProb> res = new ArrayList<ActionProb>();
        double uniformMax = 1. / (double) numMax;
        for (int i = 0; i < qValues.size(); i++) {
            QValue q = qValues.get(i);
            double p = 0.;
            if (q.q == maxQ) {
                p = uniformMax;
            }
            ActionProb ap = new ActionProb(q.a, p);
            res.add(ap);
        }

        return res;
    }

    @Override
    public boolean definedFor(State s) {
        return true; //can always find q-values with default value
    }
    
    public Episode rolloutPolicy(State initialState, SampleModel model) {      
        SimulatedEnvironment se = new SimulatedEnvironment(model, initialState);
        Episode ea = new Episode(initialState);
        List<State> states = new ArrayList<State>();
        ArrayList<Action> actions = new ArrayList();
        states.add(initialState);
        State currentState = initialState;
        while (!model.terminal(currentState)) {
            Action nextAction = this.action(currentState);
            actions.add(nextAction);
//            System.out.println("Executing: " + nextAction.actionName());
            EnvironmentOutcome eo = se.executeAction(nextAction);
            ea.transition(nextAction, eo.op, eo.r);
            actions.add(nextAction);
            currentState = eo.op;
        }
        return ea;
    }
    
//    public void rolloutPolicy(State initialState, Termination tf) {
//        List<State> states = new ArrayList<State>();
//        ArrayList<Action> actions = new ArrayList();
//        states.add(initialState);
//        State currentState = initialState;
//        while (tf.)
//    }

}
