package sim.control;

import java.util.Random;
import javax.imageio.*;
import java.io.*;

import java.awt.Point;
import java.awt.Dimension;
import java.awt.image.Raster;
import java.awt.image.BufferedImage;

import sim.util.Logger;
import sim.model.Agent.MovementBehavior;
import sim.model.Mall;
import sim.model.Board;
import sim.model.Cell;
import sim.model.Agent;
import sim.model.algo.Ped4;
import sim.model.algo.SocialForce;
import sim.model.helpers.Misc;

public class ResourceManager {

    /**
     * Loads shopping mall data from an image file.
     */
    public void loadShoppingMall(String filename) {
        Logger.log("Loading mall: " + filename);

        BufferedImage bi = null;
        Raster img = null;
        Cell[][] grid = null;

        int h = 0;
        int w = 0;

        try {
            bi = ImageIO.read(new File(filename));
            img = bi.getData();

            h = img.getHeight();
            w = img.getWidth();
            int[] pixel = new int[3];

            grid = new Cell[h][w];

            Logger.log("Creating board...");

            for(int i = 0; i < h; ++i) {
                for(int j = 0; j < w; ++j) {
                    img.getPixel(j, i, pixel);

                    switch(pixel[0]) {
                        case 0:
                            grid[i][j] = Cell.WALL;
                        break;
                        case 127:
                            grid[i][j] = new Cell(Cell.Type.PASSABLE, Ped4.getInstance());
                        break;
                        default:
                            grid[i][j] = new Cell(Cell.Type.PASSABLE, SocialForce.getInstance());
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        Board b = new Board(grid);
        Mall.getInstance().setBoard(b);
        Logger.log("Board created!");

        Logger.log("Randomizing board...");

        randomize(b, h*w/25);

        Logger.log("Board randomized!");

        Logger.log("Mall loaded!");
    }

    // TODO
    public Agent loadAgent(String filename) {
        return new Agent(MovementBehavior.DYNAMIC);
    }

    private void randomize(Board b, int nAgents) {
        Random r = new Random();
        Dimension d = b.getDimension();

        for (int i = 0; i < nAgents; i++) {
            Point p = new Point(r.nextInt(d.width), r.nextInt(d.height));

            if(b.getCell(p).isPassable()) {
                Misc.setAgent(new Agent(MovementBehavior.DYNAMIC), p);
            }
        }

    }
}