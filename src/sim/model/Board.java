package sim.model;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Map;

import sim.model.algo.Ped4;
import sim.model.helpers.MyPoint;
import sim.model.helpers.Vec;

public class Board {
    private Cell[][] grid;

    public Board(Dimension dimension) {
        grid = new Cell[dimension.height][dimension.width];

        for (int y = 0; y < dimension.height; y++)
            for (int x = 0; x < dimension.width; x++)
                grid[y][x] = new Cell(Cell.Type.PASSABLE, Ped4.getInstance());
    }

    public Board(Cell[][] grid) {
        assert grid != null;

        this.grid = grid;
    }

    public boolean isOnBoard(Point p) {
        return p.x >= 0 && p.y >= 0 && p.x < grid[0].length && p.y < grid.length;
    }

    public Dimension getDimension() {
        return new Dimension(grid[0].length, grid.length);
    }

    public void setCell(Point p, Cell cell) {
        assert isOnBoard(p);
        assert cell != null;

        grid[p.y][p.x] = cell;
    }

    public Cell getCell(Point p) {
        assert isOnBoard(p);

        return grid[p.y][p.x];
    }

    public void print() {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                Agent w = getCell(new Point(j, i)).getAgent();

                if (w == null)
                    System.out.print('.');
                else {
                    switch (w.getDirection()) {
                    case N:
                        System.out.print('u');
                        break;
                    case E:
                        System.out.print('r');
                        break;
                    case S:
                        System.out.print('d');
                        break;
                    case W:
                        System.out.print('l');
                        break;
                    }
                }
            }
            System.out.println('|');
        }
        System.out.println('-');
    }

    /**
     * Zamienia miejscami agentów z płytek określonych przez przekazane jako
     * parametry współrzędne.
     * 
     * @param p1
     * @param p2
     */
    public void swapAgent(Point p1, Point p2) {
        Agent a = getCell(p1).getAgent();
        getCell(p1).setAgent(getCell(p2).getAgent());
        getCell(p2).setAgent(a);
    }

    public void computeForceField() {
        // Clear force field.
        for (int y = 0; y < getDimension().height; y++) {
            for (int x = 0; x < getDimension().width; x++) {
                Point curr = new Point(x, y);
                getCell(curr).setForceValue(0);
            }
        }

        for (int y = 0; y < getDimension().height; y++) {
            for (int x = 0; x < getDimension().width; x++) {
                Point curr = new Point(x, y);

                if (getCell(curr).getAgent() != null)
                    modifyForceField(curr, 1);
            }
        }
    }

    /**
     * Modyfikuje rozkład pola potencjału usuwając lub dodając wartości pola
     * danego agenta.
     * 
     * @param agentPosition
     * @param sign
     *            <code>1</code> dla dodana siły, <code>-1</code> dla odjęcia
     */
    public void modifyForceField(Point agentPosition, int sign) {
        assert (Math.abs(sign) == 1);

        Cell c = getCell(agentPosition);
        for (Map.Entry<Vec, Integer> entry : c.getAgent().getForceField().entrySet()) {
            Vec v = entry.getKey();

            switch (c.getAgent().getDirection()) {
            case N:
                v = v.rotate(0);
                break;
            case E:
                v = v.rotate(1);
                break;
            case S:
                v = v.rotate(2);
                break;
            case W:
                v = v.rotate(3);
                break;
            }

            MyPoint p = v.add(agentPosition);
            if (isOnBoard(p)) {
                getCell(p).changeForce(entry.getValue() * sign);
            }
        }
    }
    
    public void printForceField() {
        for (int y = 0; y < getDimension().height; y++) {
            for (int x = 0; x < getDimension().width; x++) {
                System.out.print(String.format("%3d", grid[y][x].getForceValue()));
            }
            System.out.println();
        }
        System.out.print("\n\n");
    }
    
}