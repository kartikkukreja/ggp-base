package kkukreja.algorithms;

import java.util.LinkedList;
import java.util.List;

import javax.naming.TimeLimitExceededException;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

/**
 * @author kartikkukreja
 * alpha beta pruning with iterative deepening for two-player competitive games
 */
public final class IterativeDeepeningAlphaBeta {
	private Role role;
	private Role opponent;
	private StateMachine theMachine;
	private LinearCombinationHeuristic heuristic;
	private boolean playerFirst; // true if in the role definition, player comes first

	public IterativeDeepeningAlphaBeta(Role role, Role opponent, StateMachine machine, LinearCombinationHeuristic heuristic, long timeout) {
		this.role = role;
		this.opponent = opponent;
		theMachine = machine;
		this.heuristic = heuristic;
		playerFirst = theMachine.getRoleIndices().get(role) == 0;
	}

	public Move nextMove (MachineState state, long timeout)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException
	{
		long start = System.currentTimeMillis();
		timeout -= 1000;

		List<Move> moves = theMachine.getLegalMoves(state, role);
		Move bestMove = moves.get(0);
		double score = 0.0;
		int depth = 0;

		if (moves.size() > 1) {
			for (depth = 1; depth < 100 && (System.currentTimeMillis()+100) < timeout; depth++) {
				try {
					double alpha = 0.0;
					Move levelMove = moves.get(0);
					for (Move move : moves) {
						double result = minscore(move, state, depth, alpha, 100.0, timeout);
						if (result > alpha) {
							alpha = result;
							levelMove = move;
						}

						if (alpha >= 100.0)
							break;
					}

					score = alpha;
					bestMove = levelMove;
					if (score >= 100.0)
						break;
				} catch (Exception e) {
					break;
				}
			}
		}

		long stop = System.currentTimeMillis();

		System.out.println("Iterative deepening alpha-beta. Move selected: " + bestMove + " Score: " + score + " Max depth reached: " + depth + " Time taken: " + (stop-start)/1000.0);
		return bestMove;
	}

	private double maxscore (MachineState state, int depth, double alpha, double beta, long maxclock)
			throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException, TimeLimitExceededException
	{
		if (theMachine.isTerminal(state))
			return theMachine.getGoal(state, role);
		if (depth <= 0)
			return heuristic.evalState(state);
		if (System.currentTimeMillis() > maxclock)
			throw new TimeLimitExceededException();

		List<Move> moves = theMachine.getLegalMoves(state, role);
		for (Move move : moves) {
			double score = minscore(move, state, depth, alpha, beta, maxclock);
			alpha = Math.max(alpha, score);
			if (alpha >= beta)
				return beta;
		}

		return alpha;
	}

	private double minscore(Move move, MachineState state, int depth, double alpha, double beta, long maxclock)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException, TimeLimitExceededException
	{
		if (System.currentTimeMillis() > maxclock)
			throw new TimeLimitExceededException();

		List<Move> moves = theMachine.getLegalMoves(state, opponent);
		for (Move oppMove : moves) {
			List<Move> jointMove = new LinkedList<Move>();
			if (playerFirst) {
				jointMove.add(move);
				jointMove.add(oppMove);
			} else {
				jointMove.add(oppMove);
				jointMove.add(move);
			}

			MachineState newstate = theMachine.getNextState(state, jointMove);
			double score = maxscore(newstate, depth-1, alpha, beta, maxclock);
			beta = Math.min(beta, score);
			if (beta <= alpha)
				return alpha;
		}

		return beta;
	}
}