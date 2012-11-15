package sim.model;

import sim.model.algo.Empty;
import sim.model.algo.MovementAlgorithm;

public class Cell {
	public static Cell WALL = new Cell(Type.BLOCKED, Empty.getInstance());

	public enum Type {
		PASSABLE, BLOCKED
	}

	private final Type type;
	private Agent agent = null;
	private MovementAlgorithm algorithm = null;

	public Cell(Type type, MovementAlgorithm algo) {
		super();
		this.type = type;
		this.algorithm = algo;
	}

	public Agent getAgent() {
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	public Type getType() {
		return type;
	}

	public MovementAlgorithm getAlgorithm() {
		return algorithm;
	}

}
