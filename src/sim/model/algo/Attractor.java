package sim.model.algo;

import sim.model.algo.MallFeature.Type;
import sim.model.Agent;

public class Attractor extends MallFeature {
    private Type type = Type.ATTRACTOR;
    private int attraction = 0;

    public Attractor(int attraction) {
        this.attraction = attraction;
    }

    public int modifyHeuristicEstimate(int score) {
        return (attraction * score) / 0x7F;
    }

    public void performAction(Agent a) {
        // Do nothing.
    }

    public Type getType() {
        return type;
    }
}