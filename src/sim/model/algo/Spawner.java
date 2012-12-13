package sim.model.algo;

import sim.model.Agent;
import sim.model.Board;

public class Spawner extends MallFeature {
    private Board board;
    private int pixelValue;

    public Spawner(Board b, int pixelValue) {
        this.board = b;
        this.pixelValue = pixelValue;
    }

    public int modifyHeuristicEstimate(int score) {
        return score;
    }

    public void performAction(Agent a) {
        assert a != null;

        board.getCell(a.getPosition()).setAgent(null);
        a.setDead(true);
    }

    public int getPixelValue() {
        return pixelValue;
    }
}
