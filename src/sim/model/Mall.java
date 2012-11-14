package sim.model;

import java.awt.Dimension;

public class Mall {
    private final Board board;

    // Default ctor
    public Mall() {
        board = new Board(new Dimension(15, 10));
    }

    public Mall(Board b) {
        board = b;
    }

    public Board getBoard() {
        return board;
    }

}
