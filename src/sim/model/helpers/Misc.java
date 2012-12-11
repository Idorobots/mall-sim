package sim.model.helpers;

import java.awt.Point;

import sim.model.Agent;
import sim.model.Board;
import sim.model.Cell;
import sim.model.Mall;

public class Misc {

    public static void setAgent(Agent a, Point p) {
        Cell c = Mall.getInstance().getBoard().getCell(p);

        if (c.getAgent() != null)
            Mall.getInstance().getBoard().modifyForceField(c.getAgent(), new MyPoint(p), -1);

        Mall.getInstance().getBoard().getCell(p).setAgent(a);
//        Mall.getInstance().getBoard().printForceField();
        
        if (a != null) {
            a.setPosition(p);
            Mall.getInstance().getBoard().modifyForceField(a, new MyPoint(p), 1);
        }
    }

//    public static void setAgentDirection(Agent a, Direction d) {
//        Mall.getInstance().getBoard().modifyForceField(a, a.-1);
//        a.setDirection(d);
//        Mall.getInstance().getBoard().modifyForceField(a, 1);
//    }


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
