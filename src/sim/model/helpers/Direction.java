package sim.model.helpers;


public enum Direction {
    N(new MyPoint(0, -1)), E(new MyPoint(1, 0)), S(new MyPoint(0, 1)), W(new MyPoint(-1, 0));

    private final MyPoint coords;


    private Direction(MyPoint coords) {
        this.coords = new MyPoint(coords);
    }


    public MyPoint getCoords() {
        return coords;
    }


    public Direction rotateLeft() {
        int len = Direction.values().length;
        for (int pos = 0; pos < len; pos++)
            if (Direction.values()[pos] == this)
                return Direction.values()[(pos - 1 + len) % len];
        throw new IllegalArgumentException();
    }


    public Direction rotateRight() {
        int len = Direction.values().length;
        for (int pos = 0; pos < len; pos++)
            if (Direction.values()[pos] == this)
                return Direction.values()[(pos + 1 + len) % len];
        throw new IllegalArgumentException();
    }


    public boolean isVertical() {
        return (this == N || this == S);
    }


    public boolean isHorizontal() {
        return (this == E || this == W);
    }


    public int diff(Direction dir) {
        int posThis = -1;
        int posDir = -1;

        for (int i = 0; i < Direction.values().length; i++) {
            if (Direction.values()[i] == this)
                posThis = i;
            if (Direction.values()[i] == dir)
                posDir = i;
        }

        assert (posThis != -1 && posDir != -1);

        return (posThis - posDir);
    }
}
