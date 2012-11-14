package sim.model;

import sim.model.algo.MovementAlgorithm;

public class Cell {
	
	public enum Type {
		FLOOR, WALL
	}
	
	private final Type type;
	private Agent agent;
	private MovementAlgorithm algorithm;

	public Cell(Type type) {
		super();
		this.type = type;
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

}
