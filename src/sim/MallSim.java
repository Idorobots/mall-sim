package sim;

import static org.monte.media.FormatKeys.EncodingKey;
import static org.monte.media.FormatKeys.FrameRateKey;
import static org.monte.media.FormatKeys.MediaTypeKey;
import static org.monte.media.VideoFormatKeys.DepthKey;
import static org.monte.media.VideoFormatKeys.ENCODING_AVI_MJPG;
import static org.monte.media.VideoFormatKeys.HeightKey;
import static org.monte.media.VideoFormatKeys.QualityKey;
import static org.monte.media.VideoFormatKeys.WidthKey;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.UIManager;

import org.monte.media.Format;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.avi.AVIWriter;
import org.monte.media.math.Rational;

import sim.control.GuiState;
import sim.control.ResourceManager;
import sim.gui.GUIBoard;
import sim.gui.MallFrame;
import sim.model.Agent;
import sim.model.Agent.MovementBehavior;
import sim.model.Board;
import sim.model.Cell;
import sim.model.Mall;
import sim.model.algo.Ped4;
import sim.model.algo.SocialForce;
import sim.model.algo.Tactical;
import sim.model.helpers.Direction;
import sim.model.helpers.Misc;

public class MallSim {

    public static long seed = 0L;
    public static Random r = new Random(seed);

    static Thread simThread = null;
    static MallFrame frame = null;

    static boolean isSuspended = false;

    /**
     * Liczba klatek symulacji na jedną klatkę animacji (zapisywanej do pliku
     * AVI).
     */
    public static int simFramesPerAviFrame = 1;
    static AVIWriter out = null;
    static Graphics2D g = null;
    static Graphics2D unclippedG = null;
    static BufferedImage img = null;
    static BufferedImage unclippedImg = null;
    static String aviFilename = "out.avi";
    static boolean isRecording = false;


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

                // ResourceManager.loadShoppingMall("./data/malls/simple2.bmp",
                // "./data/malls/simple2.bmp");
                ResourceManager.loadShoppingMall("./data/malls/gk0.bmp", "./data/malls/gk0map.bmp");
//                ResourceManager.loadShoppingMall("./data/malls/gk0_mod.bmp", "./data/malls/gk0map_mod.bmp");

                Mall mall = Mall.getInstance();

                if (frame == null) {
                    frame = new MallFrame(mall);
                }

                frame.setVisible(true);

                runAlgoTest();
            }
        });
    }


    public static void prepareAvi() throws IOException, AWTException {
        Format format = new Format(EncodingKey, ENCODING_AVI_MJPG, DepthKey, 24, QualityKey, 1f);

        // String cn =
        // frame.getBoard().getParent().getParent().getClass().toString();
        Rectangle dim = frame.getBoard().getVisibleRect();

        // Make the format more specific
        format = format.prepend(MediaTypeKey, MediaType.VIDEO, FrameRateKey, new Rational(30, 1), WidthKey, dim.width,
                HeightKey, dim.height);

        img = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
        g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Dimension unclippedDim = frame.getBoard().getSize();
        unclippedImg = new BufferedImage(unclippedDim.width, unclippedDim.height, BufferedImage.TYPE_INT_RGB);
        unclippedG = unclippedImg.createGraphics();
        unclippedG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        out = new AVIWriter(new File(aviFilename));
        out.addTrack(format);
        out.setPalette(0, img.getColorModel());

        isRecording = true;
    }


    public static void finalizeAvi() throws IOException, AWTException {
        if (out != null) {
            out.close();
        }

        isRecording = false;
    }


    /**
     * Testuje działanie algotymów ruchu.
     */
    public static void runAlgoTest() {
        Mall.getInstance().reset();
        SimLoop loop = new SimLoop(Mall.getInstance());
        loop.addObserver(frame.getBoard());
        Board b = Mall.getInstance().getBoard();
        ResourceManager.randomize(Mall.getInstance().getBoard(), b.getDimension().height * b.getDimension().width / 50);

        if (simThread != null)
            simThread.stop();
        simThread = new Thread(loop);
        simThread.start();

        if (isSuspended)
            simThread.suspend();
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
                            final double maxDistanceFromTarget = 2;
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
            Point p = new Point();
            Set<Agent> moved = new HashSet<Agent>();
            for (int step = 0; step < Agent.V_MAX; step++) {
                moved.clear();
                for (int y = 0; y < board.getDimension().height; y++) {
                    for (int x = 0; x < board.getDimension().width; x++) {
                        p.setLocation(x, y);
                        Agent a = board.getCell(p).getAgent();

                        if (a == null || moved.contains(a))
                            continue;

                        if (a.getHoldTime() > 0) {
                            a.decrementHoldTime();
                            continue;
                        }

                        // Agent osiągnął swój końcowy cel.
                        if (a.getTargetCount() == 0 || a.getTarget().equals(p))
                            continue;

                        moved.add(a);

                        if (speedPointsLeft.get(a) > 0) {
                            // XXX: w przyszłości można tu dodać model
                            // probabilistyczny (aby uzyskać w miarę
                            // równomierny rozkład wykonanych kroków w
                            // czasie)

                            board.getCell(p).getAlgorithm().nextIterationStep(a, speedPointsLeft);
                            board.getCell(a.getPosition()).getFeature().performAction(a);

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
            final int LOOPS = 10;
            final int STEPS = 5000;

            // Liczba poprawnie zakończonych iteracji (wszystkie cele
            // osiągnięte).
            int nSuccesses = 0;

            int nTotalAgents = 0;
            int nAgentSuccesses = 0;

            System.out.println(board.countAgents());

            loop: for (int lp = 0; lp < LOOPS; lp++) {

                // testSocialForce(board);
                // testPed4(board);

                Point p = new Point();
                for (int y = 0; y < board.getDimension().height; y++)
                    for (int x = 0; x < board.getDimension().width; x++) {
                        p.setLocation(x, y);
                        board.getCell(p).clearVisitsCounter();
                    }

                testTactical(board);

                int nAgentsBegin = board.countAgents();
                nTotalAgents += nAgentsBegin;

                // System.out.println("begin = " + nAgentsBegin);

                // Ilość agentów, którzy osiągnęli swój cel.
                int targetsReached = 0;

                for (int i = 0; i < STEPS; i++) {
                    if (isRecording && i % simFramesPerAviFrame == 0) {
                        frame.getBoard().paint(unclippedG);

                        Rectangle r = frame.getBoard().getVisibleRect();

                        int x, y;
                        x = (r.x + img.getWidth() <= unclippedImg.getWidth()) ? r.x : unclippedImg.getWidth()
                                - img.getWidth();
                        y = (r.y + img.getHeight() <= unclippedImg.getHeight()) ? r.y : unclippedImg.getHeight()
                                - img.getHeight();

                        img = unclippedImg.getSubimage(x, y, img.getWidth(), img.getHeight());
                        try {
                            out.write(0, img, 1);
                        } catch (IOException e) {
                            System.err.println("AVI write");
                            e.printStackTrace();

                            try {
                                finalizeAvi();
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                    }

                    targetsReached = computeTargetReached();

                    if (targetsReached == nAgentsBegin) {
                        nSuccesses++;
                        nAgentSuccesses += nAgentsBegin;
                        continue loop;
                    }

                    try {
                        Thread.sleep(GuiState.animationSpeed);
                    } catch (InterruptedException e) {
                    }

                    prepareAgents();

                    Map<Agent, Integer> speedPointsLeft = computeMovementPointsLeft();
                    moveAgents(speedPointsLeft);

                    // assert (nAgentsBegin == board.countAgents());
                    // System.out.println(String.format("i[%d] = %d", i,
                    // board.countAgents()));
                }

                // System.out.println("end = " + board.countAgents());

                nAgentSuccesses += targetsReached;
            }

            System.out.println(String.format("Sukcesy pętli:  \t %d / %d\t (%d%%)", nSuccesses, LOOPS, nSuccesses * 100
                    / LOOPS));
            System.out.println(String.format("Sukcesy agentów:\t %d / %d\t (%d%%)", nAgentSuccesses, nTotalAgents,
                    nAgentSuccesses * 100 / nTotalAgents));
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
            Agent a1 = new Agent(MovementBehavior.DYNAMIC);
            a1.addTarget(new Point(3, 6));
            a1.setInitialDistanceToTarget(new Point(5, 6).distance(new Point(3, 6)));
            Misc.setAgent(a1, new Point(5, 6));
        } else if (mode == WALK_AROUND) {
            Agent a1 = new Agent(MovementBehavior.DYNAMIC);
            a1.addTarget(new Point(12, 3));
            a1.setInitialDistanceToTarget(new Point(2, 6).distance(new Point(12, 3)));
            Misc.setAgent(a1, new Point(2, 6));

            Agent a2 = new Agent(MovementBehavior.DYNAMIC);
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
                Agent a = new Agent(MovementBehavior.DYNAMIC);
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

        Agent a = new Agent(MovementBehavior.DYNAMIC);
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
                Agent a = new Agent(MovementBehavior.DYNAMIC);
                a.addTarget(new Point(r.nextInt(d.width), r.nextInt(d.height)));
                a.setInitialDistanceToTarget(p.distance(a.getTarget()));
                a.setDirection(Direction.values()[r.nextInt(Direction.values().length)]);
                Misc.setAgent(a, p);
            }
        }
    }


    private static void testTactical(Board board) {
        Tactical tactical = new Tactical(board);
        // tactical.useMooreNeighbourhood(false);

        if (board.countAgents() == 0) {
            Misc.setAgent(new Agent(MovementBehavior.DYNAMIC), new Point(2, 2));
        }

        for (int y = 0; y < board.getDimension().height; y++) {
            for (int x = 0; x < board.getDimension().width; x++) {
                Point p = new Point(x, y);
                Agent a = board.getCell(p).getAgent();
                if (a != null) {
                    a.clearTargets();
                    tactical.initializeTargets(a);
                }
            }
        }
    }


    synchronized public static void setThreadState(boolean _isSuspended) {
        isSuspended = _isSuspended;

        if (isSuspended)
            simThread.suspend();
        else
            simThread.resume();
    }


    synchronized public static boolean getThreadState() {
        return isSuspended;
    }


    // public static Thread getThread() {
    // return simThread;
    // }

    public static void reseed() {
        r = new Random(seed);
    }
    
    public static GUIBoard getGUIBoard() {
        return (frame != null) ? frame.getBoard() : null;
    }
}
