package sim.model.algo;

import sim.model.Board;

public abstract class MovementAlgorithm {

	static enum Algorithm {
		PED_4, SOCIAL_FORCE
	}

	protected final Board board;

	protected MovementAlgorithm(Board board) {
		super();
		this.board = board;
	}

	abstract public void nextIterationStep();
}
