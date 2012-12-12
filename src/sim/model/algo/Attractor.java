package sim.model.algo;

import sim.model.Agent;

public class Attractor extends MallFeature {
    private int attraction = 0;
    private int holdTime = 0;

    public Attractor(int attraction, int holdTime) {
        this.attraction = attraction;
        this.holdTime = holdTime;
    }

    public int modifyHeuristicEstimate(int score) {
        return (attraction * score) / 0x7F;
    }

    public void performAction(Agent a) {
        // Hold agent for some time...
    }
}