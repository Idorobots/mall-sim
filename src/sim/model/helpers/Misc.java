package sim.model.helpers;

import java.awt.Point;

import sim.model.Agent;
import sim.model.Board;
import sim.model.Mall;

public class Misc {

    public static void setAgent(Agent a, Point p) {
        Mall.getInstance().getBoard().getCell(p).setAgent(a);

        if (a != null)
            a.setPosition(p);
    }


    /**
     * Zamienia miejscami agentów z płytek określonych przez przekazane jako
     * parametry współrzędne.
     * 
     * @param p1
     * @param p2
     */
    public static void swapAgent(Point p1, Point p2) {
        Board b = Mall.getInstance().getBoard();

        Agent a1 = b.getCell(p1).getAgent();
        Agent a2 = b.getCell(p2).getAgent();

        setAgent(a1, p2);
        setAgent(a2, p1);
    }

}
