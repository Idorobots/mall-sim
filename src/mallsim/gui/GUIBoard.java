package mallsim.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

import sim.model.Board;
import sim.model.Cell;

@SuppressWarnings("serial")
public class GUIBoard extends JComponent implements MouseInputListener,
		ComponentListener {

	private Board board;

	private int cellSize = 14;

	public GUIBoard(Board board) {
		this.board = board;
		initialize();
	}

	private void initialize() {
		addMouseListener(this);
		addComponentListener(this);
		addMouseMotionListener(this);
		setBackground(Color.WHITE);
		setOpaque(true);

		setPreferredSize(new Dimension(board.getDimension().width * cellSize
				+ 1, board.getDimension().height * cellSize + 1));
	}

	protected void paintComponent(Graphics g) {
		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
		}

		g.setColor(Color.GRAY);
		drawNetting(g, cellSize);
	}

	private void drawNetting(Graphics g, int gridSpace) {
		Insets insets = getInsets();
		int firstX = insets.left;
		int firstY = insets.top;
		int lastX = this.getWidth() - insets.right;
		int lastY = this.getHeight() - insets.bottom;

		int x = firstX;
		while (x < lastX) {
			g.drawLine(x, firstY, x, lastY);
			x += gridSpace;
		}

		int y = firstY;
		while (y < lastY) {
			g.drawLine(firstX, y, lastX, y);
			y += gridSpace;
		}

		for (y = 0; y < board.getDimension().height; y++) {
			for (x = 0; x < board.getDimension().width; x++) {
				Cell c = board.getCell(new Point(x, y));

				switch (c.getType()) {
				case FLOOR:
					g.setColor(Color.WHITE);
					break;
				case WALL:
					g.setColor(Color.BLACK);
					break;
				}

				if (c.getAgent() != null) {
					g.setColor(Color.BLUE);
				}

				g.fillRect((x * cellSize) + 1, (y * cellSize) + 1,
						(cellSize - 1), (cellSize - 1));
			}
		}

	}

	public void mouseClicked(MouseEvent e) {
		/*
		 * int x = e.getX() / size; int y = e.getY() / size; if ((x <
		 * points.length) && (x > 0) && (y < points[x].length) && (y > 0)) {
		 * points[x][y].clicked(); this.repaint(); }
		 */
	}

	public void componentResized(ComponentEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		/*
		 * int x = e.getX() / size; int y = e.getY() / size; if ((x <
		 * points.length) && (x > 0) && (y < points[x].length) && (y > 0)) {
		 * points[x][y].setState(1); this.repaint(); }
		 */
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

}
