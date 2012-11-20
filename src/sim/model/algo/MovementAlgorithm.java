package sim.model.algo;

import java.awt.Point;
import java.util.Map;

import sim.model.Agent;
import sim.model.Board;

public interface MovementAlgorithm {

    public static enum Algorithm {
        NONE, PED_4, SOCIAL_FORCE
    }

    /**
     * 
     * @param board
     *            plansza
     * @param p
     *            punkt na planszy, dla którego wykonujemy algorytm
     */
    public abstract void prepare(Board board, Point p);

    /**
     * 
     * @param board
     *            plansza
     * @param p
     *            punkt na planszy, dla którego wykonujemy algorytm
     * @param mpLeft
     *            mapa określająca ilość jeszcze niewykorzystanych punktów ruchu
     *            agentów
     */
    public abstract void nextIterationStep(Board board, Point p, Map<Agent, Integer> mpLeft);
}
