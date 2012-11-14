package sim.control;

import java.util.Random;
import javax.imageio.*;
import java.io.*;

import java.awt.Point;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import sim.model.Mall;
import sim.model.Board;
import sim.model.Cell;
import sim.model.Agent;

public class ResourceManager {

    /**
     * Loads shopping mall data from an image file.
     */
    public Mall loadShoppingMall(String filename) {
        BufferedImage img = null;
        Board board = null;

        try {
            img = ImageIO.read(new File(filename));
            int h = img.getHeight();
            int w = img.getWidth();

            board = new Board(new Dimension(w, h));

            for(int i = 0; i < h; ++i) {
                for(int j = 0; j < w; ++j) {
                    int[] pixel = img.getData().getPixel(j, i, (int[]) null);

                    if(pixel[0] == 0) {
                        board.setCell(new Point(j, i), Cell.WALL);
                    }

                    // TODO Dispatch on the pixel value:
                    // TODO Holders
                    // TODO Queues
                    // TODO Attractors

                }
            }

            randomize(board);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return new Mall(board);
    }

    // TODO
    public Agent loadAgent(String filename) {
        return new Agent();
    }


    private void randomize(Board b) {
        Random r = new Random();
        Dimension d = b.getDimension();
        for (int i = 0; i < 500; i++) {
            Cell c = b.getCell(new Point(r.nextInt(d.width), r.nextInt(d.height)));

            if(c != Cell.WALL)
              c.setAgent(new Agent());
        }

    }
}