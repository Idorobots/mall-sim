package sim.model;

import java.awt.Point;

import sim.model.helpers.Direction;

public class Agent {
    private Point position;
    private int vMax;
    private int vCurr;
    private Direction direction;

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getvMax() {
        return vMax;
    }

    public int getvCurr() {
        return vCurr;
    }

    public void setvCurr(int vCurr) {
        this.vCurr = vCurr;
    }

}
