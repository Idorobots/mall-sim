package sim.model;

import java.awt.Dimension;

public class Mall {
	private static Mall instance = new Mall();

	private final Board board;

	// Default ctor
	public Mall() {
		board = new Board(new Dimension(15, 10));
	}

	public Mall(Board b) {
		board = b;
	}

	public static Mall getInstance() {
		return instance;
	}

	public Board getBoard() {
		return board;
	}

}
