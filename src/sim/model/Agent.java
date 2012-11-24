package sim.model;

import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import sim.model.helpers.Direction;
import sim.model.helpers.Vec;

public class Agent {

    /**
     * The "absolute" maximum speed (number of tiles per second) a pedestrian
     * can cover in one iteration, currently ~6.5km/h.
     */
    public static final int V_MAX = 6;

    public static final int FORCE_VALUE_MAX = -5;

    private int vMax;

    /**
     * Kierunek ruchu.
     */
    private Direction direction = Direction.N;

    /**
     * Punkt, do którego aktualnie zmierza agent.
     */
    private List<Point> route;

    /**
     * Mapowanie [punkt]->[wartość pola potencjału].
     */
    private Map<Vec, Integer> forceField;

    /**
     * Ilość płytek przebytych, by dotrzeć do obecnego celu.
     */
    private int fieldsMoved = 0;

    /**
     * Dystans do celu w chwili ustalenia celu.
     */
    private double initialDistanceToTarget = 0;


    public Agent() {
        vMax = 1;
        route = new LinkedList<Point>();
        initForceField();
    }


    private void initForceField() {
        forceField = new HashMap<Vec, Integer>();

        int level = Integer.MAX_VALUE;

        // za agentem
        level = 1;
        forceField.put(new Vec(-1, level), -2);
        forceField.put(new Vec(0, level), -3);
        forceField.put(new Vec(1, level), -2);

        // obok agenta
        level = 0;
        forceField.put(new Vec(-2, level), -2);
        forceField.put(new Vec(-1, level), -4);
        forceField.put(new Vec(1, level), -4);
        forceField.put(new Vec(2, level), -2);

        // +1 przed agentem
        level = -1;
        forceField.put(new Vec(-2, level), -2);
        forceField.put(new Vec(-1, level), -4);
        forceField.put(new Vec(0, level), -5);
        forceField.put(new Vec(1, level), -4);
        forceField.put(new Vec(2, level), -2);

        // +2 przed agentem
        level = -2;
        forceField.put(new Vec(-2, level), -1);
        forceField.put(new Vec(-1, level), -3);
        forceField.put(new Vec(0, level), -4);
        forceField.put(new Vec(1, level), -3);
        forceField.put(new Vec(2, level), -1);

        // +3 przed agentem
        level = -3;
        forceField.put(new Vec(-1, level), -1);
        forceField.put(new Vec(0, level), -2);
        forceField.put(new Vec(1, level), -1);
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


    public int getTargetCount() {
        return route.size();
    }


    public void reachTarget() {
        if (!route.isEmpty())
            route.remove(0);
        fieldsMoved = 0;
    }


    public Map<Vec, Integer> getForceField() {
        // TODO: immutable map?
        return forceField;
    }


    public int getFieldsMoved() {
        return fieldsMoved;
    }


    public void incrementFieldsMoved() {
        fieldsMoved++;
    }


    public double getInitialDistanceToTarget() {
        return initialDistanceToTarget;
    }


    public void setInitialDistanceToTarget(double initialDistanceToTarget) {
        this.initialDistanceToTarget = initialDistanceToTarget;
    }

}
