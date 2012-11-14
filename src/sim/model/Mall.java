package sim.model;

import java.awt.Dimension;

public class Mall {
	private final Board board;

	public Mall() {
		board = new Board(new Dimension(15, 10));
	}

	public Board getBoard() {
		return board;
	}

}
