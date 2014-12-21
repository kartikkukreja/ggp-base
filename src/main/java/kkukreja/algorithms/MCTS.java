package kkukreja.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public final class MCTS {
	private Role[] roles;
	private int playerIndex;
	private int maxRoleIndex;
	private StateMachine theMachine;
	private Random theRandom = new Random();
	private MachineState simulationState;

	public MCTS (StateMachine theMachine, List<Role> roles, Role role, long timeout)
			throws MoveDefinitionException, TransitionDefinitionException
	{
		this.theMachine = theMachine;
		this.roles = new Role[roles.size()];
		this.roles = roles.toArray(this.roles);
		this.maxRoleIndex = this.roles.length - 1;
		this.playerIndex = theMachine.getRoleIndices().get(role);
	}

	public Move nextMove (MachineState state, long timeout)
			throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException
	{
		long start = System.currentTimeMillis();

		List<Move> jointMove = new ArrayList<Move>(roles.length);
		for (int i = 0; i < roles.length; i++)
			jointMove.add(null);

		MCTSNode root = new MCTSNode(null, jointMove, 0, roles.length);
		double avStepTime = 0;
		int numSteps = 0;
		while (true) {
			long before = System.currentTimeMillis();
			if (before + avStepTime > timeout)
				break;
			doMCTS(state, root);
			long after = System.currentTimeMillis();
			avStepTime = (avStepTime * numSteps * 0.97 + (after - before)) / (numSteps + 1);
			numSteps++;
		}

		MCTSNode node = root;
		for (int i = 0; i <= playerIndex; i++) {
			double maxscore = -1;
			MCTSNode bestchild = null;
			for (MCTSNode child : node.children) {
				double util = child.utility[node.index];
				if (util > maxscore) {
					maxscore = util;
					bestchild = child;
				}
			}

			node = bestchild;
		}

		Move selection = node.jointMove.get(playerIndex);

		long stop = System.currentTimeMillis();

		System.out.println("MCTS iterations: " + root.visits + " av utility: " + Arrays.toString(root.utility) + " Time taken: " + (stop-start)/1000.0);
		return selection;
	}

	private MCTSNode select (MCTSNode node) throws TransitionDefinitionException {
		if (node.visits == 0 || node.children.size() == 0)
			return node;

		double score = -1.0;
		MCTSNode result = node;
		for (MCTSNode child : node.children) {
			double newscore = selectfn(child);
			if (newscore > score) {
				score = newscore;
				result = child;
			}
		}

		if (result.index == 0)
			simulationState = theMachine.getNextStateDestructively(simulationState, result.jointMove);
		return select(result);
	}

	private double selectfn (MCTSNode node) {
		if (node.visits == 0)
			return 1000 + theRandom.nextDouble();
		return (node.utility[node.parent.index] / 100.0) + 2*Math.sqrt(Math.log(node.parent.visits) / node.visits);
	}

	// Sparsely populate nodes until a new state (all players having made moves)
	// Returns a randomly selected child node, which can be later simulated from
	private MCTSNode expand (MCTSNode node) throws MoveDefinitionException, TransitionDefinitionException {
		List<Move> moves = theMachine.getLegalMoves(simulationState, roles[node.index]);
		for (Move move : moves) {
			List<Move> jointMove = new ArrayList<Move>(node.jointMove);
			jointMove.set(node.index, move);
			MCTSNode newnode = new MCTSNode(node, jointMove, (node.index == maxRoleIndex) ? 0 : node.index+1, roles.length);
			node.children.add(newnode);
		}

		if (node.index < maxRoleIndex)
			return expand(node.children.get(theRandom.nextInt(node.children.size())));
		else {
			MCTSNode selection = node.children.get(theRandom.nextInt(node.children.size()));
			simulationState = theMachine.getNextStateDestructively(simulationState, selection.jointMove);
			return selection;
		}
	}

	private void backpropagate (MCTSNode node, List<Integer> utility) {
		int i = 0;
		for (int util : utility) {
			node.utility[i] = (node.utility[i] * node.visits + util) / (node.visits + 1);
			i++;
		}

		node.visits++;
		if (node.parent != null)
			backpropagate(node.parent, utility);
	}

	private void doMCTS (MachineState state, MCTSNode node)
			throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException
	{
		simulationState = state;
		MCTSNode selection = select(node);

		if (selection.index == 0 && theMachine.isTerminal(simulationState)) {
			backpropagate(selection, theMachine.getGoals(simulationState));
			return;
		}

		// perform random exploration from only one child
		MCTSNode child = expand(selection);
		backpropagate(child, theMachine.simulate(simulationState));
	}
}