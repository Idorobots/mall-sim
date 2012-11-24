package sim.model.helpers;

import java.awt.Point;

@SuppressWarnings("serial")
public class Vec extends MyPoint {
    public Vec(Point p) {
        super(p);
    }
    
    public Vec(int x, int y) {
        super(x, y);
    }

    public Vec rotate(int n) {
        final int nRotationStates = 4;
        assert (n >= -3 && n <= 3);
        n = (n+nRotationStates)%nRotationStates;
        
        Vec v = new Vec(this);
        
        while (n-- > 0)
            v = v.rotateCW();
        return v;
    }

    /**
     * Obrót o 90 st. zgodnie z ruchem wskazówek zegara.
     * 
     * @return
     */
    public Vec rotateCW() {
        return new Vec(-y, x);
    }


    public Vec mul(int k) {
        return new Vec(x * k, y * k);
    }
    
    public Vec add(Vec v) {
        return new Vec(x + v.x, y + v.y);
    }
    
}
