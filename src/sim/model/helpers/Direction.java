package sim.model.helpers;


public enum Direction {
    N(new Vec(0, -1), 0.0d), E(new Vec(1, 0), 90.0d), S(new Vec(0, 1), 180.0d), W(new Vec(-1, 0), 270.0d);

    /**
     * Kierunek na planszy (zmiana współrzędnych płytki).
     */
    private final Vec coords;
    
    /**
     * Kąt (w stopniach) między kierunkiem, a północą.
     */
    private final double azimuth;


    private Direction(Vec coords, double azimuth) {
        this.coords = new Vec(coords);
        this.azimuth = azimuth;
    }


    public Vec getVec() {
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


    public double getAzimuth() {
        return azimuth;
    }
    
    
}
