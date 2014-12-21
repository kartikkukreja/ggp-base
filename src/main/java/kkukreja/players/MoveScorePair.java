package kkukreja.players;

import org.ggp.base.util.statemachine.Move;

public final class MoveScorePair {
	public Move move;
	public int score;
	public MoveScorePair (Move move, int score) {
		this.move = move;
		this.score = score;
	}
}