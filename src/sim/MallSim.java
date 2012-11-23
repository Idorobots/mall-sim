package sim;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.io.IOException;
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
import sim.model.algo.SocialForce;
import sim.model.helpers.Direction;

public class MallSim {

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
                
//                runResourceTest();
                runAlgoTest();
            }
        });
    }

    /**
     * Testuje ładowanie mapy galerii z pliku.
     */
    private static void runResourceTest() {
        ResourceManager resMgr = new ResourceManager();
        Mall mall = resMgr.loadShoppingMall("./data/malls/huge.bmp");
//        Mall mall = resMgr.loadShoppingMall("./data/malls/1floor.bmp");
//        Mall mall = resMgr.loadShoppingMall("./data/malls/simple.bmp");
        MallFrame frame = new MallFrame(mall);
        frame.setVisible(true);
    }
    
    /**
     * Testuje działanie algotymów ruchu.
     */
    private static void runAlgoTest() {
        Mall mall = new Mall();
        MallFrame frame = new MallFrame(mall);
        frame.setVisible(true);

        SimLoop loop = new SimLoop(mall);
        loop.addObserver(frame.getBoard());
        Thread t = new Thread(loop);
        t.start();
    }

    private static class SimLoop extends Observable implements Runnable {

        private Mall mall;

        public SimLoop(Mall mall) {
            super();
            this.mall = mall;
        }

        @Override
        public void run() {
            final int LOOPS = 1;
            final int STEPS = 20;
            final int DELAY = 100;

            for (int lp = 0; lp < LOOPS; lp++) {

                testSocialForce(mall.getBoard());
                // testPed4(mall.getBoard());

                mall.getBoard().computeForceField();
                // mall.getBoard().printForceField();

                int nAgentsBegin = 0;
                int nAgentsEnd = 0;
                for (int y = 0; y < mall.getBoard().getDimension().height; y++) {
                    for (int x = 0; x < mall.getBoard().getDimension().width; x++) {
                        if (mall.getBoard().getCell(new Point(x, y)).getAgent() != null)
                            nAgentsBegin++;
                    }
                }

                for (int i = 0; i < STEPS; i++) {

                    // Sprawdzanie, czy cel został osiągnięty.
                    for (int y1 = 0; y1 < mall.getBoard().getDimension().height; y1++) {
                        for (int x1 = 0; x1 < mall.getBoard().getDimension().width; x1++) {
                            Agent a = mall.getBoard().getCell(new Point(x1, y1)).getAgent();

                            if (a != null && a.getTarget().equals(new Point(x1, y1)))
                                a.reachTarget();
                        }
                    }

                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException e) {
                    }

                    // Movement
                    for (int y = 0; y < mall.getBoard().getDimension().height; y++) {
                        for (int x = 0; x < mall.getBoard().getDimension().width; x++) {
                            Point p = new Point(x, y);
                            Cell c = mall.getBoard().getCell(p);

                            if (c.getAgent() != null && !c.getAgent().getTarget().equals(p))
                                c.getAlgorithm().prepare(mall.getBoard(), p);
                        }
                    }

                    this.setChanged();
                    this.notifyObservers();

                    // Obliczenie ilości pozostałych punktów ruchu dla agentów.
                    Map<Agent, Integer> speedPointsLeft = new WeakHashMap<Agent, Integer>();
                    for (int y = 0; y < mall.getBoard().getDimension().height; y++) {
                        for (int x = 0; x < mall.getBoard().getDimension().width; x++) {
                            Point p = new Point(x, y);
                            Agent a = mall.getBoard().getCell(p).getAgent();

                            if (a != null)
                                speedPointsLeft.put(a, a.getvMax());
                        }
                    }

                    for (int step = 0; step < Agent.V_MAX; step++) {
                        Set<Agent> moved = new HashSet<Agent>();
                        for (int y = 0; y < mall.getBoard().getDimension().height; y++) {
                            for (int x = 0; x < mall.getBoard().getDimension().width; x++) {
                                Point p = new Point(x, y);
                                Agent a = mall.getBoard().getCell(p).getAgent();

                                if (a == null || moved.contains(a))
                                    continue;

                                if (a.getTarget().equals(p))
                                    continue;

                                moved.add(a);

                                if (speedPointsLeft.get(a) > 0) {
                                    // XXX: w przyszłości można tu dodać model
                                    // probabilistyczny (aby uzyskać w miarę
                                    // równomierny rozkład wykonanych kroków w
                                    // czasie)

                                    speedPointsLeft.put(a, speedPointsLeft.get(a) - 1);
                                    mall.getBoard().getCell(p).getAlgorithm()
                                            .nextIterationStep(mall.getBoard(), p, speedPointsLeft);

                                    nAgentsEnd = 0;
                                    for (int yin = 0; yin < mall.getBoard().getDimension().height; yin++) {
                                        for (int xin = 0; xin < mall.getBoard().getDimension().width; xin++) {
                                            Cell c = mall.getBoard().getCell(new Point(xin, yin));
                                            if (c.getAgent() != null)
                                                nAgentsEnd++;

                                            // Agent nie może znajdować się na
                                            // niedostępnym polu.
                                            assert (!(!c.isPassable() && c.getAgent() != null));
                                        }
                                    }

                                    // if (nAgentsBegin != nAgentsEnd) {
                                    // System.out.println(String.format(
                                    // "[%d %d]", x, y));
                                    // System.out.println(nAgentsBegin + " "
                                    // + nAgentsEnd);
                                    // throw new AssertionError();
                                    // }
                                }
                            }
                        }
                    }

                    this.setChanged();
                    this.notifyObservers();

                    // mall.getBoard().print();

                }

            }

            System.exit(0);
        }

    }

    private static void testSocialForce(Board b) {
        final int N_AGENTS = 20;
        Random r = new Random();
        Dimension d = b.getDimension();

        for (int y = 0; y < b.getDimension().height; y++)
            for (int x = 0; x < b.getDimension().width; x++) {
                Point p = new Point(x, y);
                b.getCell(p).setAgent(null);
                b.getCell(p).setAlgorithm(SocialForce.getInstance());
            }

        Agent a1 = new Agent();
        a1.addTarget(new Point(12, 3));
        b.getCell(new Point(2, 6)).setAgent(a1);

        Agent a2 = new Agent();
        a2.addTarget(new Point(7, 5));
        b.getCell(new Point(7, 5)).setAgent(a2);

        // Point p = new Point((int) d.getWidth() / 2, (int) d.getHeight() / 2);
        //
        // Agent a1 = new Agent();
        // a1.setDirection(Direction.W);
        // b.getCell(p).setAgent(a1);
        //
        // Agent a2 = new Agent();
        // a2.setDirection(Direction.N);
        // b.getCell(new Point(p.x - 1, p.y + 1)).setAgent(a2);

        // for (int i = 0; i < N_AGENTS; i++) {
        // Cell c = b.getCell(new Point(r.nextInt(d.width),
        // r.nextInt(d.height)));
        //
        // if (c != Cell.WALL) {
        // Agent a = new Agent();
        // a.setDirection(Direction.values()[r.nextInt(Direction.values().length)]);
        // c.setAgent(a);
        // }
        // }

    }

    private static void testPed4(Board b) {
        final int N_AGENTS = 20;
        Random r = new Random();
        Dimension d = b.getDimension();

        for (int y = 0; y < b.getDimension().height; y++)
            for (int x = 0; x < b.getDimension().width; x++)
                b.getCell(new Point(x, y)).setAgent(null);

        for (int y = 3; y < 8; y++)
            for (int x = 5; x < 10; x++)
                ;
        // b.setCell(new Point(x, y), Cell.WALL);

        Agent a = new Agent();
        a.addTarget(new Point(8, 5));
        a.addTarget(new Point(4, 1));
        b.getCell(new Point(0, 0)).setAgent(a);

        // Point p = new Point((int) d.getWidth() / 2, (int) d.getHeight() / 2);
        //
        // Agent a1 = new Agent();
        // a1.setDirection(Direction.W);
        // b.getCell(p).setAgent(a1);
        //
        // Agent a2 = new Agent();
        // a2.setDirection(Direction.N);
        // b.getCell(new Point(p.x - 1, p.y + 1)).setAgent(a2);

        // for (int i = 0; i < N_AGENTS; i++) {
        // Cell c = b.getCell(new Point(r.nextInt(d.width),
        // r.nextInt(d.height)));
        //
        // if (c != Cell.WALL) {
        // Agent a = new Agent();
        // a.setDirection(Direction.values()[r.nextInt(Direction.values().length)]);
        // c.setAgent(a);
        // }
        // }

    }
}
