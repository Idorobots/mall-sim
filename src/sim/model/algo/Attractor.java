package sim.model.algo;

import sim.model.Agent;

public class Attractor extends MallFeature {
    private int attraction = 0;
    private int holdTime = 0;
    private int pixelValue = 0xffffff;

    // TODO Compute attraction and holdTime from pv.
    public Attractor(int attraction, int holdTime, int pv) {
        this.attraction = attraction;
        this.holdTime = holdTime;
        this.pixelValue = pv;
    }

    public int modifyHeuristicEstimate(int score) {
        return (attraction * score) / 0x7F;
    }

    public void performAction(Agent a) {
        a.setHoldTime(holdTime);
    }

    public int getPixelValue() {
        return pixelValue;
    }
}