package sim.model;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import sim.model.helpers.Direction;

public class Agent {
    /**
     * The "absolute" maximum speed (number of tiles per second) a pedestrian
     * can cover in one iteration, currently ~6.5km/h.
     */
    public static final int V_MAX = 6;

    private Point position;
    private int vMax;

    /**
     * Kierunek ruchu.
     */
    private Direction direction = Direction.N;

    /**
     * Punkt, do którego aktualnie zmierza agent.
     */
    private List<Point> route;

    public Agent() {
        vMax = 2;
        route = new LinkedList<Point>();
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

    public Point getTarget() {
        return route.get(0);
    }

    public void addTarget(Point target) {
        route.add(target);
    }

    public void reachTarget() {
        assert (!route.isEmpty());
        route.remove(0);
    }
}
