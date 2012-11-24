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
	public void prepare(Agent a) {
	}

	@Override
	public void nextIterationStep(Agent a, Map<Agent, Integer> mpLeft) {
	}

}
