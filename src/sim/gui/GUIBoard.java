package sim.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

import sim.gui.helpers.Helpers;
import sim.model.Agent;
import sim.model.Board;
import sim.model.Cell;

@SuppressWarnings("serial")
public class GUIBoard extends JComponent implements MouseInputListener, MouseWheelListener, ComponentListener, Observer {

    private Board board;

    private int cellSize = 14;
    private int agentSize = 16;

    private int resizeDelta = 1; // Used for dynamic resizing.

    boolean showForceField = false;
    boolean showTargetLines = false;


    public GUIBoard(Board board) {
        this.board = board;
        initialize();
    }


    public boolean isShowForceField() {
        return showForceField;
    }


    public void setShowForceField(boolean showForceField) {
        this.showForceField = showForceField;
    }


    public boolean isShowTargetLines() {
        return showTargetLines;
    }


    public void setShowTargetLines(boolean showTargetLines) {
        this.showTargetLines = showTargetLines;
    }


    private void initialize() {
        addMouseListener(this);
        addComponentListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        setBackground(Color.WHITE);
        setOpaque(true);

        setPreferredSize(new Dimension(board.getDimension().width * cellSize + 1, board.getDimension().height
                * cellSize + 1));
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

        Map<Point, Point> targets = new HashMap<Point, Point>();

        for (y = 0; y < board.getDimension().height; y++) {
            for (x = 0; x < board.getDimension().width; x++) {
                Cell c = board.getCell(new Point(x, y));

                switch (c.getType()) {
                case PASSABLE:
                    if (showForceField)
                        g.setColor(getPassableColor(c));
                    else
                        g.setColor(Color.WHITE);
                    break;
                case BLOCKED:
                    g.setColor(Color.BLACK);
                    break;
                }

                g.fillRect((x * cellSize) + 1, (y * cellSize) + 1, (cellSize - 1), (cellSize - 1));

                Agent a = c.getAgent();

                if (a != null) {
                    drawAgent(g, a, x, y);

                    if (a.getTargetCount() > 0)
                        targets.put(new Point(x, y), a.getTarget());
                }
            }
        }

        // Draw target lines.
        if (showTargetLines) {
            g.setColor(Color.CYAN);
            Graphics2D g2d = (Graphics2D) g;
            Stroke s = g2d.getStroke();
            g2d.setStroke(new BasicStroke(3f));  // set stroke width of 10
            for (Map.Entry<Point, Point> entry : targets.entrySet()) {
                int x1 = entry.getKey().x * cellSize + cellSize / 2;
                int y1 = entry.getKey().y * cellSize + cellSize / 2;
                int x2 = entry.getValue().x * cellSize + cellSize / 2;
                int y2 = entry.getValue().y * cellSize + cellSize / 2;

                g.drawLine(x1, y1, x2, y2);
            }
            g2d.setStroke(s);
        }
    }


    private Color getPassableColor(Cell c) {
        int forceValue = c.getForceValue();

        if (c.getAgent() != null || forceValue == 0)
            return Color.WHITE;

        int maxSummedForceValue = 8 * Agent.FORCE_VALUE_MAX;
        int coef = 255 * forceValue / maxSummedForceValue;

        // Ujemny współczynnik powstaje np. gdy ludzie długo chodzą w kółko.
        // FIXME Bugs like crazy.
        try {
            return (coef > 0) ? new Color(coef, 255 - coef, 0) : new Color(-coef, 0, 255 + coef);
        }
        catch(Exception e) {
            //System.out.println(String.format("%d, %d, %d", coef, 255-coef, 0));
            //System.out.println(String.format("%d, %d, %d", -coef, 0, 255+coef));
        }
        return Color.MAGENTA;
    }


    // Convinience method for the soon to be lengthy Agent drawing code.
    private void drawAgent(Graphics g, Agent a, int x, int y) {
        final int DIRECTION_MARKER_LENGTH = 10;

        assert a != null;
        assert g != null;

        g.setColor(Color.BLUE);

        int agentH = 0;
        int agentW = 0;
        int agentHeadSize = agentSize / 3;

        switch (a.getDirection()) {
        case N:
        case S:
            agentH = agentSize / 2;
            agentW = agentSize;
            break;
        case E:
        case W:
            agentH = agentSize;
            agentW = agentSize / 2;
            break;
        }

        int agentX = x * cellSize + cellSize / 2;
        int agentY = y * cellSize + cellSize / 2;

        g.fillOval(agentX - agentW / 2, agentY - agentH / 2, agentW, agentH);

        g.setColor(Color.GREEN);
        g.drawLine(agentX, agentY, agentX + a.getDirection().getVec().x * DIRECTION_MARKER_LENGTH, agentY
                + a.getDirection().getVec().y * DIRECTION_MARKER_LENGTH);

        g.setColor(Color.YELLOW);
        g.fillOval(agentX - agentHeadSize / 2, agentY - agentHeadSize / 2, agentHeadSize, agentHeadSize);
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


    // Half-assed dynamic resizing. Still needs some work. FIXME
    public void mouseWheelMoved(MouseWheelEvent e) {
        int rotation = e.getWheelRotation();

        if (rotation != 0) {
            cellSize = Helpers.clamp(cellSize + resizeDelta * rotation, 3, 25);
            agentSize = Helpers.clamp(agentSize + resizeDelta * rotation, 2, 27);

            Dimension d = new Dimension(board.getDimension().width * cellSize + 1, board.getDimension().height
                    * cellSize + 1);

            this.setSize(d);

            repaint();

            // FIXME Adjust underlaying ScrollPane accordingly.
        }
    }


    @Override
    public void update(Observable o, Object arg) {
        repaint();
    }

}
