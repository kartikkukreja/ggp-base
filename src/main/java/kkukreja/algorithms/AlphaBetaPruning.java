package kkukreja.algorithms;

import java.util.LinkedList;
import java.util.List;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

/**
 * @author kartikkukreja
 * alpha beta pruning for two-player competitive games
 * Currently does not take "timeout" into consideration. Assumes that the entire
 * game tree can be searched in the available time.
 */
public final class AlphaBetaPruning implements SearchAlgorithm {
	private Role role;
	private Role opponent;
	private StateMachine theMachine;
	private boolean playerFirst; // true if in the role definition, player comes first

	public AlphaBetaPruning(Role role, Role opponent, StateMachine machine, long timeout) {
		this.role = role;
		this.opponent = opponent;
		theMachine = machine;
		playerFirst = theMachine.getRoleIndices().get(role) == 0;
	}

	@Override
	public Move nextMove (MachineState state)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException
	{
		List<Move> moves = theMachine.getLegalMoves(state, role);
		Move bestMove = moves.get(0);
		int score = 0;

		if (moves.size() > 1) {
			for (Move move : moves) {
				int result = minscore(move, state, 0, 100);
				if (result > score) {
					score = result;
					bestMove = move;
				}
			}
		}

		return bestMove;
	}

	private int maxscore (MachineState state, int alpha, int beta)
			throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException
	{
		if (theMachine.isTerminal(state))
			return theMachine.getGoal(state, role);

		List<Move> moves = theMachine.getLegalMoves(state, role);
		for (Move move : moves) {
			int score = minscore(move, state, alpha, beta);
			alpha = Math.max(alpha, score);
			if (alpha >= beta)
				return beta;
		}

		return alpha;
	}

	private int minscore(Move move, MachineState state, int alpha, int beta)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException
	{
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
			int score = maxscore(newstate, alpha, beta);
			beta = Math.min(beta, score);
			if (beta <= alpha)
				return alpha;
		}

		return beta;
	}
}