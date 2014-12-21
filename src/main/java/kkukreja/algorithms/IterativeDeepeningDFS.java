package kkukreja.algorithms;

import java.util.ArrayList;
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
 * DFS with iterative deepening for puzzles
 */
public final class IterativeDeepeningDFS {
	private Role role;
	private StateMachine theMachine;
	private LinearCombinationHeuristic heuristic;

	public IterativeDeepeningDFS(Role role, StateMachine machine, LinearCombinationHeuristic heuristic, long timeout) {
		this.role = role;
		theMachine = machine;
		this.heuristic = heuristic;
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
					double levelScore = 0.0;
					Move levelMove = moves.get(0);
					for (Move move : moves) {
						double result = minscore(move, state, depth, timeout);
						if (result > levelScore) {
							levelScore = result;
							levelMove = move;
						}

						if (levelScore >= 100.0)
							break;
					}

					score = levelScore;
					bestMove = levelMove;
					if (score >= 100.0)
						break;
				} catch (Exception e) {
					break;
				}
			}
		}

		long stop = System.currentTimeMillis();

		System.out.println("Iterative deepening DFS. Move selected: " + bestMove + " Score: " + score + " Max depth reached: " + depth + " Time taken: " + (stop-start)/1000.0);
		return bestMove;
	}

	private double maxscore (MachineState state, int depth, long maxclock)
			throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException, TimeLimitExceededException
	{
		if (theMachine.isTerminal(state))
			return theMachine.getGoal(state, role);
		if (depth <= 0)
			return heuristic.evalState(state);
		if (System.currentTimeMillis() > maxclock)
			throw new TimeLimitExceededException();

		List<Move> moves = theMachine.getLegalMoves(state, role);
		double bestScore = 0.0;
		for (Move move : moves) {
			double score = minscore(move, state, depth, maxclock);
			if (score > bestScore)
				bestScore = score;
			if (bestScore >= 100.0)
				return 100.0;
		}

		return bestScore;
	}

	private double minscore(Move move, MachineState state, int depth, long maxclock)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException, TimeLimitExceededException
	{
		if (System.currentTimeMillis() > maxclock)
			throw new TimeLimitExceededException();

		List<Move> jointMove = new ArrayList<Move>();
		jointMove.add(move);
		return maxscore(theMachine.getNextState(state, jointMove), depth-1, maxclock);
	}
}