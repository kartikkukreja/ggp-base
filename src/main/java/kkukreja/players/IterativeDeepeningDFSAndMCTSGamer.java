package kkukreja.players;

import java.util.List;

import kkukreja.algorithms.IterativeDeepeningDFS;
import kkukreja.algorithms.LinearCombinationHeuristic;
import kkukreja.algorithms.MCTS;

import org.ggp.base.apps.player.detail.DetailPanel;
import org.ggp.base.apps.player.detail.SimpleDetailPanel;
import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

public final class IterativeDeepeningDFSAndMCTSGamer extends StateMachineGamer
{
	private MCTS mctsGamer = null;
	private IterativeDeepeningDFS dfsGamer = null;
	private int rolecount;

	@Override
	public String getName() {
		return "dragonlord";
	}

	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		long start = System.currentTimeMillis();

		List<Move> moves = getStateMachine().getLegalMoves(getCurrentState(), getRole());
		Move selection = null;
		if (moves.size() == 1)
			selection = moves.get(0);
		else
			selection = (rolecount == 1)
				? dfsGamer.nextMove(getCurrentState(), timeout-1000)
				: mctsGamer.nextMove(getCurrentState(), timeout-1000);

		long stop = System.currentTimeMillis();

		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;
	}

	@Override
	public StateMachine getInitialStateMachine() {
		return new CachedStateMachine(new ProverStateMachine());
	}

	@Override
	public void preview(Game g, long timeout) throws GamePreviewException {
		// does no game previewing.
	}

	@Override
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		StateMachine theMachine = getStateMachine();
		List<Role> roles = theMachine.getRoles();
		rolecount = roles.size();
		Role role = getRole();

		if (rolecount == 1) {
			LinearCombinationHeuristic heuristic = Util.findBestHeuristic(theMachine, role, timeout);
			dfsGamer = new IterativeDeepeningDFS(role, theMachine, heuristic, timeout - 1000);
		} else
			mctsGamer = new MCTS(theMachine, roles, role, timeout - 1000);
	}

	@Override
	public void stateMachineStop() {
		if (rolecount == 1)
			dfsGamer = null;
		else
			mctsGamer = null;
	}

	@Override
	public void stateMachineAbort() {
		if (rolecount == 1)
			dfsGamer = null;
		else
			mctsGamer = null;
	}

	@Override
	public DetailPanel getDetailPanel() {
		return new SimpleDetailPanel();
	}
}