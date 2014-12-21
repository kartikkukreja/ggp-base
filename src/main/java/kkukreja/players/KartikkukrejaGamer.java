package kkukreja.players;

import java.util.List;
import java.util.Random;

import kkukreja.algorithms.AlphaBetaPruning;
import kkukreja.algorithms.IterativeDeepeningAlphaBeta;
import kkukreja.algorithms.IterativeDeepeningDFS;
import kkukreja.algorithms.LinearCombinationHeuristic;
import kkukreja.algorithms.NaiveDFS;

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

public class KartikkukrejaGamer extends StateMachineGamer
{
	private GameType gametype = null;
	private NaiveDFS solveSmallPuzzle = null;
	private IterativeDeepeningDFS solveLargePuzzle = null;
	private AlphaBetaPruning solveSmall2Player = null;
	private IterativeDeepeningAlphaBeta solveLarge2Player = null;
	private StateMachine theMachine = null;
	private List<Role> roles = null;
	private Role role = null;
	private Random theRandom = new Random();

	@Override
	public String getName() {
		return "kartikkukreja_old";
	}

	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		long start = System.currentTimeMillis();

		List<Move> moves = getStateMachine().getLegalMoves(getCurrentState(), getRole());
		Move selection = null;

		switch (gametype) {
		case SmallPuzzle:
			selection = solveSmallPuzzle.nextMove(getCurrentState());
			break;
		case LargePuzzle:
			selection = solveLargePuzzle.nextMove(getCurrentState(), timeout - 1000);
			break;
		case Small2Player:
			selection = solveSmall2Player.nextMove(getCurrentState());
			break;
		case Large2Player:
			selection = solveLarge2Player.nextMove(getCurrentState(), timeout - 1000);
			break;
		default:
			selection =  moves.get(theRandom.nextInt(moves.size()));
		}

		long stop = System.currentTimeMillis();

		System.out.println("Move selected : " + selection + " Time taken : " + ((stop-start)/1000.0));

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
		timeout -= 1000;
		theMachine = getStateMachine();
		roles = theMachine.getRoles();
		role = getRole();
		int playerCount = roles.size();

		if (playerCount > 2)
			gametype = GameType.MultiPlayer;
		else if (playerCount == 1)
			gametype = GameType.LargePuzzle;
		else
			gametype = GameType.Large2Player;

		switch (gametype) {
		case SmallPuzzle:
			solveSmallPuzzle = new NaiveDFS(theMachine, timeout);
			break;
		case LargePuzzle:
			LinearCombinationHeuristic heuristic = Util.findBestHeuristic(theMachine, role, timeout);
			solveLargePuzzle = new IterativeDeepeningDFS(role, theMachine, heuristic, timeout);
			break;
		case Small2Player:
			Role opponent = null;
			for (Role r : roles) {
				if (!r.equals(role)) {
					opponent = r;
					break;
				}
			}

			solveSmall2Player = new AlphaBetaPruning(role, opponent, theMachine, timeout);
			break;
		case Large2Player:
			opponent = null;
			for (Role r : roles) {
				if (!r.equals(role)) {
					opponent = r;
					break;
				}
			}

			heuristic = Util.findBestHeuristic(theMachine, role, timeout);
			solveLarge2Player = new IterativeDeepeningAlphaBeta(role, opponent, theMachine, heuristic, timeout);
			break;
		default:
			break;
		}

		System.out.println("Game type : " + gametype);
	}

	@Override
	public void stateMachineStop() {
		switch (gametype) {
		case SmallPuzzle:
			solveSmallPuzzle = null;
			break;
		case LargePuzzle:
			solveLargePuzzle = null;
			break;
		case Small2Player:
			solveSmall2Player = null;
			break;
		case Large2Player:
			solveLarge2Player = null;
			break;
		default:
			break;
		}

		gametype = null;
		theMachine = null;
		roles = null;
		role = null;
	}

	@Override
	public void stateMachineAbort() {
		switch (gametype) {
		case SmallPuzzle:
			solveSmallPuzzle = null;
			break;
		case LargePuzzle:
			solveLargePuzzle = null;
			break;
		case Small2Player:
			solveSmall2Player = null;
			break;
		case Large2Player:
			solveLarge2Player = null;
			break;
		default:
			break;
		}

		gametype = null;
		theMachine = null;
		roles = null;
		role = null;
	}

	@Override
	public DetailPanel getDetailPanel() {
		return new SimpleDetailPanel();
	}
}