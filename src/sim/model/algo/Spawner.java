package sim.model.algo;

import sim.model.Agent;
import sim.model.helpers.Misc;

public class Spawner extends MallFeature {
    private int pixelValue;

    public Spawner(int pixelValue) {
        this.pixelValue = pixelValue;
    }

    public int modifyHeuristicEstimate(int score) {
        return score;
    }

    public void performAction(Agent a) {
        assert a != null;

        Misc.setAgent(null, a.getPosition());
        a.setDead(true);
    }

    public int getPixelValue() {
        return pixelValue;
    }
}
