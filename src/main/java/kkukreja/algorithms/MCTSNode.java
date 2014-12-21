package kkukreja.algorithms;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.statemachine.Move;

public class MCTSNode {
	public int visits, index;
	public double[] utility;
	public List<MCTSNode> children;
	public MCTSNode parent;
	public List<Move> jointMove;

	public MCTSNode() {
		visits = 0;
		index = -1;
		utility = null;
		children = null;
		parent = null;
		jointMove = null;
	}

	public MCTSNode (MCTSNode parent, List<Move> move, int index, int roleCount) {
		this.parent = parent;
		this.index = index;
		jointMove = move;
		children = new ArrayList<MCTSNode>();
		utility = new double[roleCount];
		visits = 0;
	}

	// only for 2-player games
	@Override
	public String toString() {
		return String.format("[visits=%d index=%d utility=[%f,%f] children.size=%s jointMove=%s]",
				visits, index, utility[0], utility[1], children.size(), jointMove);
	}
}