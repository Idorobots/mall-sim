package sim.model.algo;

import sim.model.Agent;

public abstract class MallFeature {
    public static enum Type {
        HOLDER, QUEUE, ATTRACTOR
    }

    public abstract int modifyHeuristicEstimate(int score);

    public abstract void performAction(Agent a);
}