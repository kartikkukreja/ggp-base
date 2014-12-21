package kkukreja.algorithms;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import kkukreja.players.MoveScorePair;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

/**
 * @author kartikkukreja
 * Sequential deliberation for small single player games. The call to the constructor
 * completely solves the game and creates a plan, which is then followed move by move.
 * Naive DFS, redundancy of computation can be avoided by storing previously encountered
 * states in a hashtable.
 */
public final class NaiveDFS implements SearchAlgorithm {
	private Role role;
	private StateMachine theMachine;
	private ArrayDeque<MoveScorePair> plan;
	private boolean planBuilt;

	private ArrayDeque<MoveScorePair> buildPlan (MachineState state, long timeout)
			throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException, TimeoutException
	{
		if ((System.currentTimeMillis()+100) > timeout) {
			System.out.println("Time limit exceeded while building plan.");
			throw new TimeoutException();
		}

		if (theMachine.isTerminal(state)) {
			int score = theMachine.getGoal(state, role);
			ArrayDeque<MoveScorePair> plan = new ArrayDeque<MoveScorePair>();
			plan.push(new MoveScorePair(null, score));
			return plan;
		}

		List<Move> moves = theMachine.getLegalMoves(state, role);
		ArrayDeque<MoveScorePair> bestPlan = null;
		Move bestMove = null;
		int bestScore = -1;

		for (Move move : moves) {
			List<Move> jointMove = new LinkedList<Move>();
			jointMove.add(move);
			MachineState nextState = theMachine.getNextState(state, jointMove);
			ArrayDeque<MoveScorePair> plan = buildPlan(nextState, timeout);
			MoveScorePair lastMove = plan.element();
			if (lastMove.score > bestScore) {
				bestScore = lastMove.score;
				bestMove = move;
				bestPlan = plan;
			}
		}

		if (bestPlan == null)
			bestPlan = new ArrayDeque<MoveScorePair>();

		bestPlan.push(new MoveScorePair(bestMove, bestScore));
		return bestPlan;
	}

	public NaiveDFS (StateMachine machine, long timeout)
			throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException
	{
		theMachine = machine;
		role = theMachine.getRoles().get(0);
		planBuilt = true;

		try {
			System.out.println("Trying to build a plan...");
			plan = buildPlan(theMachine.getInitialState(), timeout);
			System.out.println("Complete plan built successfully.");
		} catch (TimeoutException e) {
			System.out.println("Failed to build complete plan under time limit.");
			planBuilt = false;
		}
	}

	@Override
	public Move nextMove (MachineState state)
			throws MoveDefinitionException
	{
		if (!planBuilt) {
			// return a random move
			return theMachine.getRandomMove(state, role);
		}

		return plan.pop().move;
	}
}