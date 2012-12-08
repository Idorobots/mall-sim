package sim.model;

import java.awt.Point;

import sim.model.algo.Empty;
import sim.model.algo.MovementAlgorithm;
import sim.model.algo.MallFeature;
import sim.model.helpers.MyPoint;

public class Cell {
    public static final Cell WALL = new Cell(Type.BLOCKED, Empty.getInstance());

    public enum Type {
        PASSABLE, BLOCKED
    }

    private final Type type;
    private Agent agent = null;
    private MovementAlgorithm algorithm = null;
    private MallFeature feature = null;

    private int forceValue;
    private int forceValue4Rendering;

    public Cell(Type type, MovementAlgorithm algo, MallFeature feature) {
        this(type, algo);
        setFeature(feature);
    }

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

    public MallFeature getFeature() {
        return feature;
    }

    public void setFeature(MallFeature feature) {
        this.feature = feature;
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

    public int getForceValue4Rendering() {
        return forceValue4Rendering;
    }


    public void setForceValue(int forceValue) {
        this.forceValue = forceValue;
    }

    public void flipForceValue() {
        forceValue4Rendering = forceValue;
    }

    void changeForce(int forceValue) {
        this.forceValue += forceValue;
    }

    // XXX: debug
    public void setAlgorithm(MovementAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

}
