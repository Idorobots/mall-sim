package sim.model;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Observable;
import java.util.Observer;

import sim.model.algo.Ped4;

public class Board {
    private Cell[][] grid;

    public Board(Dimension dimension) {
        grid = new Cell[dimension.height][dimension.width];

        for (int y = 0; y < dimension.height; y++)
            for (int x = 0; x < dimension.width; x++)
                grid[y][x] = new Cell(Cell.Type.PASSABLE, Ped4.getInstance());
    }

    public boolean isOnBoard(Point p) {
        return p.x >= 0 && p.y >= 0 && p.x < grid[0].length
                && p.y < grid.length;
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

}
