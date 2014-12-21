package kkukreja.players;

import java.util.List;
import java.util.Random;

import kkukreja.algorithms.IterativeDeepeningDFS;
import kkukreja.algorithms.LinearCombinationHeuristic;

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

public final class IterativeDeepeningDFSGamer extends StateMachineGamer
{
	private IterativeDeepeningDFS gamer = null;
	private Random theRandom = new Random();
	private int rolecount;

	@Override
	public String getName() {
		return "IterativeDeepeningDFS_GAMER";
	}

	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		long start = System.currentTimeMillis();

		List<Move> moves = getStateMachine().getLegalMoves(getCurrentState(), getRole());
		Move selection = (rolecount == 1) ? gamer.nextMove(getCurrentState(), timeout - 1000) : moves.get(theRandom.nextInt(moves.size()));

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
		rolecount = theMachine.getRoles().size();
		Role role = getRole();

		if (rolecount == 1) {
			LinearCombinationHeuristic heuristic = Util.findBestHeuristic(theMachine, role, timeout);
			gamer = new IterativeDeepeningDFS(role, theMachine, heuristic, timeout - 1000);
		}
	}

	@Override
	public void stateMachineStop() {
		gamer = null;
	}

	@Override
	public void stateMachineAbort() {
		gamer = null;
	}

	@Override
	public DetailPanel getDetailPanel() {
		return new SimpleDetailPanel();
	}
}