package mallsim.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.event.MouseInputListener;

@SuppressWarnings("serial")
public class Board extends JComponent implements MouseInputListener, ComponentListener {

    private int cellSize = 14;

    // XXX: temp
    int model[][];


    public Board(Dimension gridDimension) {
        initialize(gridDimension);
        test(gridDimension);
    }


    // XXX: temp
    private void test(Dimension gridDimension) {
        model = new int[gridDimension.height][gridDimension.width];
        Random r = new Random();
        for (int i = 0; i < 20; i++) {
            model[r.nextInt(model.length)][r.nextInt(model[0].length)] = r.nextInt(3);
        }
    }


    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                try {
                    JFrame frame = new JFrame();
                    frame.setVisible(true);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setBounds(100, 100, 450, 300);

                    Board board = new Board(new Dimension(40, 40));
                    frame.add(board);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void initialize(Dimension gridDimension) {
        addMouseListener(this);
        addComponentListener(this);
        addMouseMotionListener(this);
        setBackground(Color.WHITE);
        setOpaque(true);

        setPreferredSize(new Dimension(gridDimension.width * cellSize + 1, gridDimension.height * cellSize + 1));
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

        for (x = 0; x < model.length; x++) {
            for (y = 0; y < model[x].length; y++) {
                switch (model[x][y]) {
                case 0:
                    g.setColor(Color.WHITE);
                    break;
                case 1:
                    g.setColor(new Color(0x0000ff));
                    break;
                case 2:
                    g.setColor(new Color(0x00ff00));
                    break;
                case 3:
                    g.setColor(new Color(0xff0000));
                    break;
                }
                g.fillRect((x * cellSize) + 1, (y * cellSize) + 1, (cellSize - 1), (cellSize - 1));
            }
        }

    }


    public void mouseClicked(MouseEvent e) {
        /*        int x = e.getX() / size;
                int y = e.getY() / size;
                if ((x < points.length) && (x > 0) && (y < points[x].length) && (y > 0)) {
                    points[x][y].clicked();
                    this.repaint();
                }*/
    }


    public void componentResized(ComponentEvent e) {
    }


    public void mouseDragged(MouseEvent e) {
        /*        int x = e.getX() / size;
                int y = e.getY() / size;
                if ((x < points.length) && (x > 0) && (y < points[x].length) && (y > 0)) {
                    points[x][y].setState(1);
                    this.repaint();
                }*/
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
