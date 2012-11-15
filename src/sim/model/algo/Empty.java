package sim.model.algo;

import java.awt.Point;
import java.util.Map;

import sim.model.Agent;
import sim.model.Board;

public class Empty implements MovementAlgorithm {

	private static MovementAlgorithm instance = new Empty();

	private Empty() {
	}

	public static MovementAlgorithm getInstance() {
		return instance;
	}

	@Override
	public void prepare(Board board, Point p) {
	}

	@Override
	public void nextIterationStep(Board board, Point p, Map<Agent, Integer> mpLeft) {
	}

}
