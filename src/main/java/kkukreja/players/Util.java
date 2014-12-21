package kkukreja.players;

import java.util.List;
import java.util.Random;

import kkukreja.algorithms.LinearCombinationHeuristic;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public final class Util {

	/**
	 * @return (av. branching factor) ^ (av. game tree depth) for 20 randomly played games
	 * @throws TransitionDefinitionException
	 * @throws MoveDefinitionException
	 */
	public static double complexityOfGameTree (StateMachine theMachine)
			throws MoveDefinitionException, TransitionDefinitionException
	{
		MachineState initialState = theMachine.getInitialState();
		Random theRandom = new Random();
		double avBranchingFactor = 0.0, avDepth = 0.0;
		int numNodes = 0;
		final int iterations = 20;

		for (int i = 0; i < iterations; i++) {
			MachineState state = initialState;
			while (!theMachine.isTerminal(state)){
				List<List<Move>> moves = theMachine.getLegalJointMoves(state);
				avBranchingFactor += moves.size();
				numNodes++;
				state = theMachine.getNextState(state, moves.get(theRandom.nextInt(moves.size())));
			}
		}

		avBranchingFactor /= numNodes;
		avDepth = numNodes / (double) iterations;
		return Math.pow(avBranchingFactor, avDepth);
	}

	/**
	 * plays the game with several randomly chosen heuristic combinations to find the best one
	 * @param theMachine
	 * @return the best LinearCombinationHeuristic seen during simulated playoffs against random player
	 * @throws TransitionDefinitionException
	 * @throws MoveDefinitionException
	 * @throws GoalDefinitionException
	 */
	public static LinearCombinationHeuristic findBestHeuristic (StateMachine theMachine, Role role, long timeout)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException
	{
		long start = System.currentTimeMillis();

		Random theRandom = new Random();
		int roleIndex = theMachine.getRoleIndices().get(role);
		MachineState initialState = theMachine.getInitialState();
		LinearCombinationHeuristic bestHeuristic = new LinearCombinationHeuristic(theMachine, role);
		double bestScore = playoff (theMachine, role, initialState, roleIndex, bestHeuristic, theRandom) - 10;

		long timeTakenForFirstPlayOff = System.currentTimeMillis() - start;
		int totalMatchesPossible = (int) (((timeout-start) / (timeTakenForFirstPlayOff)) - 2);
		int matches, rounds;

		if (totalMatchesPossible > 10000) {
			matches = 1000; rounds = totalMatchesPossible / matches;
		} else if (totalMatchesPossible > 1000) {
			matches = 100; rounds = totalMatchesPossible / matches;
		} else if (totalMatchesPossible > 100) {
			matches = 10; rounds = totalMatchesPossible / matches;
		} else if (totalMatchesPossible > 50) {
			matches = 7; rounds = totalMatchesPossible / matches;
		} else if (totalMatchesPossible > 30) {
			matches = 5; rounds = totalMatchesPossible / matches;
		} else if (totalMatchesPossible > 10) {
			matches = 3; rounds = totalMatchesPossible / matches;
		} else if (totalMatchesPossible > 5) {
			matches = 2; rounds = totalMatchesPossible / matches;
		} else {
			matches = 1; rounds = totalMatchesPossible;
		}

		System.out.println("Total matches possible: " + totalMatchesPossible +
						" Rounds: " + rounds + " Matches per round: " + matches +
						" Time taken by first playoff: " + timeTakenForFirstPlayOff/1000.0 + " Total time: " + (timeout-start)/1000.0);

		for (int round = 0; (round < rounds || (timeout-System.currentTimeMillis()-100) > timeTakenForFirstPlayOff*matches) && bestScore < 100.0; round++) {
			LinearCombinationHeuristic newHeuristic = new LinearCombinationHeuristic(theMachine, role);
			double score = 0.0;
			for (int i = 0; i < matches; i++) {
				if ((System.currentTimeMillis()+200) > timeout)
					break;
				score += playoff (theMachine, role, initialState, roleIndex, newHeuristic, theRandom);
			}

			score /= matches;

			if (score > bestScore) {
				bestHeuristic = newHeuristic;
				bestScore = score;
			}
		}

		long stop = System.currentTimeMillis();

		System.out.println("Heuristic selected: " + bestHeuristic + " Best score: " + bestScore + " Time taken: " + (stop-start)/1000.0);
		return bestHeuristic;
	}

	/**
	 * plays the game against a random player using the given heuristic to find the outcome
	 * @param theMachine
	 * @param role
	 * @param initialState
	 * @param roleIndex
	 * @param heuristic
	 * @return
	 * @throws MoveDefinitionException
	 * @throws TransitionDefinitionException
	 * @throws GoalDefinitionException
	 *
	 * TODO: improve heuristic generation. getNextStateDestructively can't be called so freely
	 */
	public static int playoff (StateMachine theMachine, Role role, MachineState initialState, int roleIndex, LinearCombinationHeuristic heuristic, Random theRandom)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException
	{
		MachineState state = initialState;
		while (!theMachine.isTerminal(state)) {
			// find the best move against a 3-random move looking goal-directed player looking at 3 random moves according to the given heuristic
			List<Move> moves = theMachine.getLegalMoves(state, role);
			double bestScore = -1.0;
			MachineState bestNextState = null;

			for (int j = 0; j < 3; j++) {
				Move m = moves.get(theRandom.nextInt(moves.size()));
				MachineState nextState = null;
				int minscore = 101;

				for (int i = 0; i < 3; i++) {
					MachineState randomstate = theMachine.getNextStateDestructively(state, theMachine.getRandomJointMove(state, role, m));
					int score = theMachine.getGoal(randomstate, role);
					if (score < minscore) {
						minscore = score;
						nextState = randomstate;
					}
				}

				double score = heuristic.evalState(nextState);
				if (score > bestScore) {
					bestScore = score;
					bestNextState = nextState;
				}
			}
			state = bestNextState;
		}

		return theMachine.getGoal(state, role);
	}
}