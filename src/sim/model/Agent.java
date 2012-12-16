package sim.model;

import java.awt.Point;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Properties;

import sim.model.helpers.Direction;
import sim.model.helpers.MyPoint;
import sim.model.helpers.Vec;

public class Agent extends Observable {

    /**
     * Parametry agenta w odniesieniu do modelu ruchu (prędkość, zwinność itp.)
     * 
     * @author Pawel Kleczek
     * 
     */
    public static enum MovementBehavior {
        DYNAMIC("dynamic"), AVERAGE("average"), PENSIONER("pensioner");

        private final String filename;


        private MovementBehavior(String filename) {
            this.filename = filename;
        }


        public String getFilename() {
            return filename;
        }

    }

    /**
     * The "absolute" maximum speed (number of tiles per second) a pedestrian
     * can cover in one iteration, currently ~6.5km/h.
     */
    public static final int V_MAX = 6;

    public static final int FORCE_VALUE_MAX = -5;

    private int vMax;

    /**
     * Prawdopodobieństwo wystąpienia zamiany.
     */
    private double agility;

    private MyPoint position = null;

    /**
     * Kierunek ruchu.
     */
    private Direction direction = Direction.N;

    /**
     * Punkt, do którego aktualnie zmierza agent.
     */
    private List<Point> route;

    /**
     * Mapowanie [punkt]->[wartość pola potencjału].
     */
    private final Map<Vec, Integer> forceField;

    /**
     * Ilość płytek przebytych, by dotrzeć do obecnego celu.
     */
    private int fieldsMoved = 0;

    /**
     * Dystans do celu w chwili ustalenia celu.
     */
    private double initialDistanceToTarget = 0;


    /**
     * Czas wstrzymania dekrementowany co każdy krok symulacji.
     */

    private int holdTime = 0;


    /**
     * Flaga określająca, czy Agent został usunięty z symulacji.
     */
    private boolean isDead = false;

    public Agent(MovementBehavior movementBehavior) {

        try {
            loadProperties(movementBehavior);
        } catch (Exception e) {
            e.printStackTrace();

            // ustaw wartości domyślne
            vMax = 1;
            agility = 0.5;
        }

        route = new LinkedList<Point>();
        forceField = Collections.unmodifiableMap(initForceField());

        assert forceField != null;
    }


    private void loadProperties(MovementBehavior movementBehavior) throws FileNotFoundException, IOException {
        Properties prop = new Properties();
        prop.load(new FileInputStream(String.format("./data/agents/%s.agent", movementBehavior.getFilename())));

        vMax = Integer.valueOf(prop.getProperty("speed"));
        agility = Double.valueOf(prop.getProperty("agility"));
    }


    private Map<Vec, Integer> initForceField() {
        Map<Vec, Integer> tForceField = new HashMap<Vec, Integer>();

        int level = Integer.MAX_VALUE;

        // za agentem
        level = 1;
        tForceField.put(new Vec(-1, level), -2);
        tForceField.put(new Vec(0, level), -3);
        tForceField.put(new Vec(1, level), -2);

        // obok agenta
        level = 0;
        tForceField.put(new Vec(-2, level), -2);
        tForceField.put(new Vec(-1, level), -4);
        tForceField.put(new Vec(1, level), -4);
        tForceField.put(new Vec(2, level), -2);

        // +1 przed agentem
        level = -1;
        tForceField.put(new Vec(-2, level), -2);
        tForceField.put(new Vec(-1, level), -4);
        tForceField.put(new Vec(0, level), -5);
        tForceField.put(new Vec(1, level), -4);
        tForceField.put(new Vec(2, level), -2);

        // +2 przed agentem
        level = -2;
        tForceField.put(new Vec(-2, level), -1);
        tForceField.put(new Vec(-1, level), -3);
        tForceField.put(new Vec(0, level), -4);
        tForceField.put(new Vec(1, level), -3);
        tForceField.put(new Vec(2, level), -1);

        // +3 przed agentem
        level = -3;
        tForceField.put(new Vec(-1, level), -1);
        tForceField.put(new Vec(0, level), -2);
        tForceField.put(new Vec(1, level), -1);

        return tForceField;
    }


    public Direction getDirection() {
        return direction;
    }


    public void setDirection(Direction direction) {
        Mall.getInstance().getBoard().modifyForceField(this, getPosition(), -1);
        this.direction = direction;
        Mall.getInstance().getBoard().modifyForceField(this, getPosition(), 1);

        setChanged();
        notifyObservers();
    }


    public int getvMax() {
        return vMax;
    }


    public Point getTarget() {
        return route.get(0);
    }


    public List<Point> getRoute() {
        return route;
    }


    public void addTarget(Point target) {
        route.add(target);

        setChanged();
        notifyObservers();
    }


    public int getTargetCount() {
        return route.size();
    }


    public void reachTarget() {
        if (!route.isEmpty())
            route.remove(0);
        fieldsMoved = 0;

        setChanged();
        notifyObservers();
    }


    public void clearTargets() {
        route.clear();
        fieldsMoved = 0;

        setChanged();
        notifyObservers();
    }


    public Map<Vec, Integer> getForceField() {
        // TODO: immutable map?
        return forceField;
    }


    public int getFieldsMoved() {
        return fieldsMoved;
    }


    public void incrementFieldsMoved() {
        fieldsMoved++;

        setChanged();
        notifyObservers();
    }


    public double getInitialDistanceToTarget() {
        return initialDistanceToTarget;
    }


    public void setInitialDistanceToTarget(double initialDistanceToTarget) {
        this.initialDistanceToTarget = initialDistanceToTarget;

        setChanged();
        notifyObservers();
    }


    public MyPoint getPosition() {
        return new MyPoint(position);
    }


    public void setPosition(Point position) {
        this.position = new MyPoint(position);

        setChanged();
        notifyObservers();
    }


    public double getAgility() {
        return agility;
    }


    public int getHoldTime() {
        return holdTime;
    }

    public void setHoldTime(int ht) {
        holdTime = ht;

        if(holdTime < 0) holdTime = 0;

        setChanged();
        notifyObservers();
    }

    public void decrementHoldTime() {
        setHoldTime(holdTime - 1);

        setChanged();
        notifyObservers();
    }

    public void setDead(boolean state) {
        isDead = state;
    }

    public boolean getDead() {
        return isDead;
    }
}
