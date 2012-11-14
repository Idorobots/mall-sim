package sim.model;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Random;

public class Board {
    private Cell[][] grid;

    Board(Dimension dimension) {
        grid = new Cell[dimension.height][dimension.width];

        for (int y = 0; y < dimension.height; y++)
            for (int x = 0; x < dimension.width; x++)
                grid[y][x] = new Cell(Cell.Type.FLOOR);

        // XXX: TEST
        test();
    }

    private void test() {
        Random r = new Random();
        for (int i = 0; i < 20; i++) {
            Cell c = grid[r.nextInt(getDimension().height)][r
                    .nextInt(getDimension().width)];

            c.setAgent(new Agent());
        }

    }

    public boolean isOnBoard(Point p) {
        return p.x >= 0 && p.y >= 0 && p.x < grid[0].length
                && p.y < grid.length;
    }

    public Dimension getDimension() {
        return new Dimension(grid[0].length, grid.length);
    }

    public Cell getCell(Point p) {
        assert isOnBoard(p);
        return grid[p.y][p.x];
    }

    void print() {
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
