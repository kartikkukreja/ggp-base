package kkukreja.algorithms;

import java.util.HashSet;
import java.util.Random;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;

public class LinearCombinationHeuristic {
	private static int NUM_COMPONENTS = 5;
	private static double[] UPPER_BOUNDS = {0.1, 0.1, 0.2, 0.1, 0.5};
	private static int MAX_POSSIBLE_STATES = 50;
	private static int MAX_POSSIBLE_ACTIONS = 50;

	private StateMachine theMachine;
	private Role role;
	private double[] weights;
	private Random theRandom = new Random();

	public LinearCombinationHeuristic (StateMachine theMachine, Role role) {
		this.theMachine = theMachine;
		this.role = role;
		weights = new double[NUM_COMPONENTS];

		double sum = 0.0;
		for (int i = 0; i < NUM_COMPONENTS; i++) {
			weights[i] = theRandom.nextDouble() * UPPER_BOUNDS[i];
			sum += weights[i];
		}

		sum = 1.0 / sum;
		for (int i = 0; i < NUM_COMPONENTS; i++)
			weights[i] *= sum;
	}

	public LinearCombinationHeuristic (StateMachine theMachine, Role role, int maxPossibleStates, int maxPossibleActions) {
		this(theMachine, role);
		MAX_POSSIBLE_ACTIONS = maxPossibleActions;
		MAX_POSSIBLE_STATES = maxPossibleStates;
	}

	public LinearCombinationHeuristic (StateMachine theMachine, Role role, double[] weights) {
		this.theMachine = theMachine;
		this.role = role;
		this.weights = weights.clone();
	}

	public double evalState (MachineState state) {
		double actionMobility = ActionMobility(state), stateMobility = StateMobility(state);
		double eval = weights[0] * actionMobility
					+ weights[1] * (100.0 - actionMobility)
					+ weights[2] * stateMobility
					+ weights[3] * (100.0 - stateMobility)
					+ weights[4] * GoalValue(state);
		return eval;
	}

	private double ActionMobility (MachineState state) {
		try {
			int numActions = theMachine.getLegalMoves(state, role).size();
			return (numActions * 100.0) / MAX_POSSIBLE_ACTIONS;
		} catch (Exception e) {
			return 0;
		}
	}

	@SuppressWarnings("unused")
	private double ActionFocus (MachineState state) {
		try {
			return (100.0 - ActionMobility(state));
		} catch (Exception e) {
			return 0;
		}
	}

	private double StateMobility (MachineState state) {
		try {
			int numReachableStates = (new HashSet<MachineState>(theMachine.getNextStates(state))).size();
			return (numReachableStates * 100.0) / MAX_POSSIBLE_STATES;
		} catch (Exception e) {
			return 0;
		}
	}

	@SuppressWarnings("unused")
	private double StateFocus (MachineState state) {
		try {
			return (100.0 - StateMobility(state));
		} catch (Exception e) {
			return 0;
		}
	}

	private double GoalValue(MachineState state) {
		try {
			return theMachine.getGoal(state, role);
		} catch (GoalDefinitionException e) {
			System.out.println("GoalDefinitionException while finding heuristic value.");
			return 0;
		}
	}

	public double[] getWeights() {
		return weights;
	}

	@Override
	public String toString() {
		return String.format("{%f,%f,%f,%f,%f}",weights[0],weights[1],weights[2],weights[3],weights[4]);
	}
}