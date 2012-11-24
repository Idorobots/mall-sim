package sim.model.helpers;

import java.awt.Point;

public class MyPoint extends Point {

    private static final long serialVersionUID = 1L;

    public MyPoint() {
        super();
    }

    public MyPoint(Point p) {
        super(p);
    }

    public MyPoint(int x, int y) {
        super(x, y);
    }

    public MyPoint add(Vec v) {
        return new MyPoint(x + v.x, y + v.y);
    }
}
