package kkukreja.algorithms;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public interface SearchAlgorithm {
	public Move nextMove(MachineState state)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException;
}