package sim.model.algo;

import sim.model.Agent;

public abstract class MallFeature {
    public abstract int modifyHeuristicEstimate(int score);

    public abstract void performAction(Agent a);

    public int getPixelValue() {
        return 0xffffff;
    }
}