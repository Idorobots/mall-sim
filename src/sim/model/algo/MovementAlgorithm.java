package sim.model.algo;

import java.awt.Point;
import java.util.Map;

import sim.model.Agent;
import sim.model.Board;

/*
 * FIXME: problem, gdy dwóch agentów ma dokładnie tą samą płytkę-cel (jeden krąży wokół pola)
 *  rozwiązanie: wyzwolenie akcji "reachTarget" gdy odległość od celu mniejsza niż pewien threshold
 */
public interface MovementAlgorithm {

    public static enum Algorithm {
        NONE, PED_4, SOCIAL_FORCE
    }

    enum Orientation {
        SAME, OPP, ORTHO, OUT
    }


    /**
     * 
     * @param board
     *            plansza
     * @param a
     *            agent, dla którego wykonujemy algorytm
     */
    public abstract void prepare(Agent a);


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
    public abstract void nextIterationStep(Agent a, Map<Agent, Integer> mpLeft);
}
