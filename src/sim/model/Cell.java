package sim.model;

import java.awt.Point;

import sim.model.algo.Empty;
import sim.model.algo.MovementAlgorithm;
import sim.model.helpers.MyPoint;

public class Cell {
    public static final Cell WALL = new Cell(Type.BLOCKED, Empty.getInstance());

    public enum Type {
        PASSABLE, BLOCKED
    }

    private final Type type;
    private Agent agent = null;
    private MovementAlgorithm algorithm = null;

    int forceValue;

    public Cell(Type type, MovementAlgorithm algo) {
        super();
        this.type = type;
        this.algorithm = algo;
        forceValue = 0;
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

    public boolean isPassable() {
        return (type != Type.BLOCKED);
    }

    public int getForceValue() {
        return forceValue;
    }

    public void setForceValue(int forceValue) {
        this.forceValue = forceValue;
    }

    void changeForce(int forceValue) {
        this.forceValue += forceValue;
    }

    // XXX: debug
    public void setAlgorithm(MovementAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

}
