package sim.model;

import sim.model.algo.MovementAlgorithm;

public class Cell {
    public static Cell WALL = new Cell(Type.BLOCKED);

    public enum Type {
        PASSABLE, BLOCKED
    }
    
    private final Type type;
    private Agent agent = null;
    private MovementAlgorithm algorithm = null;

    public Cell() {
        this(Type.PASSABLE);
    }

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
