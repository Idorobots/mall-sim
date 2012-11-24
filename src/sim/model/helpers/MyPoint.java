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

    public MyPoint add(Point p) {
        return new MyPoint(x + p.x, y + p.y);
    }

    public MyPoint add(Vec v) {
        return new MyPoint(x + v.x, y + v.y);
    }

    public MyPoint mul(int k) {
        return new MyPoint(x * k, y * k);
    }
}
