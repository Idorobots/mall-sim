package sim.model;

import java.awt.Point;

import sim.model.helpers.Direction;

public class Agent {
	/**
	 * The "absolute" maximum speed (number of tiles per second) a pedestrian
	 * can cover in one iteration, currently ~6.5km/h.
	 */
	public static final int V_MAX = 6;

	private Point position;
	private int vMax;
	private Direction direction = Direction.N;

	public Agent() {
		vMax = 2;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public int getvMax() {
		return vMax;
	}

}
