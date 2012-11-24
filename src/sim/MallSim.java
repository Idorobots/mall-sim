package sim;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.UIManager;

import sim.control.ResourceManager;
import sim.gui.MallFrame;
import sim.model.Agent;
import sim.model.Board;
import sim.model.Cell;
import sim.model.Mall;
import sim.model.algo.Ped4;
import sim.model.algo.SocialForce;
import sim.model.helpers.Direction;
import sim.model.helpers.Misc;

public class MallSim {

    static Random r = new Random();


    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // runResourceTest();
                runAlgoTest();
            }
        });
    }


    /**
     * Testuje ładowanie mapy galerii z pliku.
     */
    private static void runResourceTest() {
        ResourceManager resMgr = new ResourceManager();
        resMgr.loadShoppingMall("./data/malls/huge.bmp");
        // resMgr.loadShoppingMall("./data/malls/1floor.bmp");
        // resMgr.loadShoppingMall("./data/malls/simple.bmp");
        MallFrame frame = new MallFrame(Mall.getInstance());
        frame.setVisible(true);
    }


    /**
     * Testuje działanie algotymów ruchu.
     */
    private static void runAlgoTest() {
        Mall mall = Mall.getInstance();
        MallFrame frame = new MallFrame(mall);
        frame.setVisible(true);

        SimLoop loop = new SimLoop(mall);
        loop.addObserver(frame.getBoard());
        Thread t = new Thread(loop);
        t.start();
    }

    private static class SimLoop extends Observable implements Runnable {

        private Mall mall;
        private Board board;


        public SimLoop(Mall mall) {
            super();
            this.mall = mall;
            board = mall.getBoard();
        }


        /**
         * Sprawdza, czy agenci dotarli do celu (jeśli tak - uaktualnia cele).
         * 
         * @return ilość agentów, którzy dotarli do celu
         */
        private int computeTargetReached() {
            int targetsReached = 0;

            // Sprawdzanie, czy cel został osiągnięty.
            for (int y1 = 0; y1 < board.getDimension().height; y1++) {
                for (int x1 = 0; x1 < board.getDimension().width; x1++) {
                    Point curr = new Point(x1, y1);
                    Agent a = board.getCell(curr).getAgent();

                    if (a != null && a.getTargetCount() > 0) {
                        if (a.getTarget().equals(curr)) {
                            a.reachTarget();
                            if (a.getTargetCount() > 0)
                                a.setInitialDistanceToTarget(curr.distanceSq(a.getTarget()));
                        } else {
                            final double maxDistanceFromTarget = 3;
                            double dist = a.getTarget().distance(curr);
                            if (dist < maxDistanceFromTarget) {
                                // TODO: metoda probabilistyczna
                                if (r.nextDouble() < 1 / (dist * dist)) {
                                    a.reachTarget();
                                    if (a.getTargetCount() > 0)
                                        a.setInitialDistanceToTarget(curr.distanceSq(a.getTarget()));
                                }
                            }
                        }
                    }

                    if (a != null && a.getTargetCount() == 0)
                        targetsReached++;
                }
            }

            return targetsReached;
        }


        private void prepareAgents() {
            // Movement
            for (int y = 0; y < board.getDimension().height; y++) {
                for (int x = 0; x < board.getDimension().width; x++) {
                    Point p = new Point(x, y);
                    Agent a = board.getCell(p).getAgent();

                    if (a != null && a.getTargetCount() > 0) {
                        if (!a.getTarget().equals(p))
                            board.getCell(p).getAlgorithm().prepare(a);
                    }

                }
            }

            this.setChanged();
            this.notifyObservers();
        }


        private Map<Agent, Integer> computeMovementPointsLeft() {
            // Obliczenie ilości pozostałych punktów ruchu dla agentów.
            Map<Agent, Integer> speedPointsLeft = new WeakHashMap<Agent, Integer>();
            for (int y = 0; y < board.getDimension().height; y++) {
                for (int x = 0; x < board.getDimension().width; x++) {
                    Point p = new Point(x, y);
                    Agent a = board.getCell(p).getAgent();

                    if (a != null)
                        speedPointsLeft.put(a, a.getvMax());
                }
            }

            return speedPointsLeft;
        }


        private void moveAgents(Map<Agent, Integer> speedPointsLeft) {
            for (int step = 0; step < Agent.V_MAX; step++) {
                Set<Agent> moved = new HashSet<Agent>();
                for (int y = 0; y < board.getDimension().height; y++) {
                    for (int x = 0; x < board.getDimension().width; x++) {
                        Point p = new Point(x, y);
                        Agent a = board.getCell(p).getAgent();

                        if (a == null || moved.contains(a))
                            continue;

                        if (a.getTargetCount() == 0 || a.getTarget().equals(p))
                            continue;

                        moved.add(a);

                        if (speedPointsLeft.get(a) > 0) {
                            // XXX: w przyszłości można tu dodać model
                            // probabilistyczny (aby uzyskać w miarę
                            // równomierny rozkład wykonanych kroków w
                            // czasie)

                            board.getCell(p).getAlgorithm().nextIterationStep(a, speedPointsLeft);
                            speedPointsLeft.put(a, speedPointsLeft.get(a) - 1);
                        }
                    }
                }
            }

            this.setChanged();
            this.notifyObservers();
        }


        @Override
        public void run() {
            final int LOOPS = 50;
            final int STEPS = 40;
            final int DELAY = 10;

            // Liczba poprawnie zakończonych iteracji (wszystkie cele
            // osiągnięte).
            int nSuccesses = 0;

            int nTotalAgents = 0;
            int nAgentSuccesses = 0;

            loop: for (int lp = 0; lp < LOOPS; lp++) {

                testSocialForce(board);
//                testPed4(board);

                board.computeForceField();

                int nAgentsBegin = board.countAgents();
                nTotalAgents += nAgentsBegin;

                // Ilość agentów, którzy osiągnęli swój cel.
                int targetsReached = 0;

                for (int i = 0; i < STEPS; i++) {
                    targetsReached = computeTargetReached();

                    if (targetsReached == nAgentsBegin) {
                        nSuccesses++;
                        nAgentSuccesses += nAgentsBegin;
                        continue loop;
                    }

                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException e) {
                    }

                    prepareAgents();
                    Map<Agent, Integer> speedPointsLeft = computeMovementPointsLeft();
                    moveAgents(speedPointsLeft);

                    assert (nAgentsBegin == board.countAgents());
                }

                nAgentSuccesses += targetsReached;

                // try {
                // // XXX: debug
                // System.in.read();
                // } catch (IOException e) {
                // }
            }

            System.out.println(String.format("Sukcesy pętli:  \t %d / %d\t (%d%%)", nSuccesses, LOOPS, nSuccesses * 100
                    / LOOPS));
            System.out.println(String.format("Sukcesy agentów:\t %d / %d\t (%d%%)", nAgentSuccesses, nTotalAgents,
                    nAgentSuccesses * 100 / nTotalAgents));

            System.exit(0);
        }

    }


    private static void testSocialForce(Board b) {
        for (int y = 0; y < b.getDimension().height; y++)
            for (int x = 0; x < b.getDimension().width; x++) {
                Point p = new Point(x, y);
                Misc.setAgent(null, p);
                b.getCell(p).setAlgorithm(SocialForce.getInstance());
            }

        // testSocialForceIndividual(b);
        testSocialForceMassive(b);
    }


    private static void testSocialForceIndividual(Board b) {
        final int TARGET_BEHIND = 1;
        final int WALK_AROUND = 2;

        int mode = TARGET_BEHIND;

        if (mode == TARGET_BEHIND) {
            Agent a1 = new Agent();
            a1.addTarget(new Point(3, 6));
            a1.setInitialDistanceToTarget(new Point(5, 6).distance(new Point(3, 6)));
            Misc.setAgent(a1, new Point(5, 6));
        } else if (mode == WALK_AROUND) {
            Agent a1 = new Agent();
            a1.addTarget(new Point(12, 3));
            a1.setInitialDistanceToTarget(new Point(2, 6).distance(new Point(12, 3)));
            Misc.setAgent(a1, new Point(2, 6));

            Agent a2 = new Agent();
            a2.addTarget(new Point(7, 5));
            a2.setInitialDistanceToTarget(new Point(7, 5).distance(new Point(7, 5)));
            Misc.setAgent(a2, new Point(7, 5));
        }
    }


    private static void testSocialForceMassive(Board b) {
        final int N_AGENTS = 10;

        Dimension d = b.getDimension();
        for (int i = 0; i < N_AGENTS; i++) {
            Point p = new Point(r.nextInt(d.width), r.nextInt(d.height));
            Cell c = b.getCell(p);

            if (c != Cell.WALL) {
                Agent a = new Agent();
                a.addTarget(new Point(r.nextInt(d.width), r.nextInt(d.height)));
                a.setInitialDistanceToTarget(p.distance(a.getTarget()));
                a.setDirection(Direction.values()[r.nextInt(Direction.values().length)]);
                Misc.setAgent(a, p);
            }
        }
    }


    private static void testPed4(Board b) {
        for (int y = 0; y < b.getDimension().height; y++)
            for (int x = 0; x < b.getDimension().width; x++) {
                Point p = new Point(x, y);
                Misc.setAgent(null, p);
                b.getCell(p).setAlgorithm(Ped4.getInstance());
            }

        // testPed4Individual(b);
        testPed4Massive(b);
    }


    private static void testPed4Individual(Board b) {
        for (int y = 0; y < b.getDimension().height; y++)
            for (int x = 0; x < b.getDimension().width; x++)
                Misc.setAgent(null, new Point(x, y));

        for (int y = 3; y < 8; y++)
            for (int x = 5; x < 10; x++)
                ;
        // b.setCell(new Point(x, y), Cell.WALL);

        Agent a = new Agent();
        a.addTarget(new Point(8, 5));
        a.setInitialDistanceToTarget(new Point(0, 0).distance(new Point(8, 5)));
        a.addTarget(new Point(4, 1));
        Misc.setAgent(a, new Point(0, 0));
    }


    private static void testPed4Massive(Board b) {
        final int N_AGENTS = 20;

        Dimension d = b.getDimension();
        for (int i = 0; i < N_AGENTS; i++) {
            Point p = new Point(r.nextInt(d.width), r.nextInt(d.height));

            if (b.getCell(p) != Cell.WALL) {
                Agent a = new Agent();
                a.addTarget(new Point(r.nextInt(d.width), r.nextInt(d.height)));
                a.setInitialDistanceToTarget(p.distance(a.getTarget()));
                a.setDirection(Direction.values()[r.nextInt(Direction.values().length)]);
                Misc.setAgent(a, p);
            }
        }
    }
}
