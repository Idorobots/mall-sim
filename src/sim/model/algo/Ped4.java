package sim.model.algo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import sim.model.Agent;
import sim.model.Board;
import sim.model.Cell;
import sim.model.Mall;
import sim.model.helpers.Direction;
import sim.model.helpers.Misc;
import sim.model.helpers.MyPoint;

// TODO: zweryfikować użycie funkcji board.swapAgent()

public final class Ped4 implements MovementAlgorithm {

    private static MovementAlgorithm instance = new Ped4();

    // FIXME: klasa nie powinna być static
    /**
     * Klasa służy do zwracania informacji z funkcji calculateGap().
     * 
     * @author Pawel
     * 
     */
    static class GapReport {

        public final Orientation direction;
        public int gap;
        public final Agent opponent;


        public GapReport(Orientation direction, int gap, Agent opponent) {
            super();
            this.direction = direction;
            this.gap = gap;
            this.opponent = opponent;
        }
    }


    private Ped4() {
    }


    public static MovementAlgorithm getInstance() {
        return instance;
    }

    // TODO: jak rozwiązać problem różnych v_max dla różnych ludzi?

    private static Random r = new Random();

    /**
     * Identyfikatory "pasów ruchu" (
     * 
     * @author Pawel Kleczek
     * 
     */
    enum Lane {
        LEFT, SAME, RIGHT
    }


    /**
     * Dostosowuje kierunek ruchu agenta z zadanego pola tak, aby kąt między
     * kierunkiem ruchu, a kierunkiem celu nie przekraczał pewnej wartości
     * progowej.
     * 
     * @param b
     * @param p
     */
    private void adjustDirection(Agent a) {
        final double MAX_ANGLE_DIFF = 60.0d;

        assert (a != null);

        // Trzeba pamiętać, że oś OY ma w programie przeciwny zwrot.
        double targetAngle = Math.toDegrees(Math.atan2(a.getTarget().x - a.getPosition().x,
                a.getPosition().y - a.getTarget().y));
        if (targetAngle < 0.0)
            targetAngle += 360.0;
        assert (targetAngle >= 0.0d && targetAngle <= 360.0d);

        if (Math.abs(targetAngle - a.getDirection().getAzimuth()) > MAX_ANGLE_DIFF) {
            if (targetAngle < 45.0d)
                a.setDirection(Direction.N);
            else if (targetAngle < 135.0d)
                a.setDirection(Direction.E);
            else if (targetAngle < 210.0d)
                a.setDirection(Direction.S);
            else if (targetAngle < 300.0d)
                a.setDirection(Direction.W);
            else
                a.setDirection(Direction.N);
        }
    }


    private Orientation getRelativeOrientation(Direction dir1, Direction dir2) {
        if (dir1 == dir2)
            return Orientation.SAME;

        return (Math.abs(dir1.diff(dir2)) == 1) ? Orientation.ORTHO : Orientation.OPP;
    }


    /**
     * Ustala (na drodze losowania), który z agentów znajdujących się na
     * sąsiedniej płytce ma prawo do zajęcia rozpatrywanej płytki.
     * <p>
     * W losowaniu biorą udział jedynie agenci odpowiednio zorientowani, którzy
     * w wyniku procedury zmiany pasa mogą się znaleźć na rozpatrywanej płytce.
     * 
     * @param board
     * @param cellCoord
     * @return <code>null</code> gdy płytka jest zajęta lub brak pretendentów
     */
    private Agent getCellAssignment(Board board, Point cellCoord) {
        List<Agent> candidates = new ArrayList<Agent>();
        Point p = null;

        // Płytka już zajęta.
        if (board.getCell(cellCoord).getAgent() != null)
            return null;

        // Wybierz tylko pieszych, których kierunek ruchu dopuszcza
        // zejśce w bok na dane pole.
        p = new Point(cellCoord.x - 1, cellCoord.y);
        if (board.isOnBoard(p) && board.getCell(p).getAgent() != null
                && board.getCell(p).getAgent().getDirection().isVertical())
            candidates.add(board.getCell(p).getAgent());

        p = new Point(cellCoord.x + 1, cellCoord.y);
        if (board.isOnBoard(p) && board.getCell(p).getAgent() != null
                && board.getCell(p).getAgent().getDirection().isVertical())
            candidates.add(board.getCell(p).getAgent());

        p = new Point(cellCoord.x, cellCoord.y - 1);
        if (board.isOnBoard(p) && board.getCell(p).getAgent() != null
                && board.getCell(p).getAgent().getDirection().isHorizontal())
            candidates.add(board.getCell(p).getAgent());

        p = new Point(cellCoord.x, cellCoord.y + 1);
        if (board.isOnBoard(p) && board.getCell(p).getAgent() != null
                && board.getCell(p).getAgent().getDirection().isHorizontal())
            candidates.add(board.getCell(p).getAgent());

        return (candidates.size() == 0) ? null : candidates.get(r.nextInt(candidates.size()));
    }


    private void changeLane(Board board, Agent w) {
        MyPoint p = w.getPosition();

        assert (w != null);

        MyPoint pleft = p.add(w.getDirection().nextCCW().getVec());
        MyPoint pright = p.add(w.getDirection().nextCW().getVec());
        GapReport gapLeft = (board.isOnBoard(pleft) && board.getCell(pleft).isPassable()) ? calculateGap(board, pleft,
                w) : null;
        GapReport gapCenter = calculateGap(board, p, w);
        GapReport gapRight = (board.isOnBoard(pright) && board.getCell(pright).isPassable()) ? calculateGap(board,
                pright, w) : null;

        if (gapLeft == null && gapRight == null)
            return;

        int gapMax = gapCenter.gap;
        if (gapLeft != null) {
            gapMax = Math.max(gapMax, gapLeft.gap);
        }
        if (gapRight != null) {
            gapMax = Math.max(gapMax, gapRight.gap);
        }

        // 2a-i
        if (gapCenter.direction == Orientation.OPP) {
            gapCenter.gap = 0;

            // 2a-ii
            List<Point> lanes = new ArrayList<Point>();
            if (gapLeft != null && 0 == gapLeft.gap && gapLeft.direction == Orientation.SAME) {
                lanes.add(pleft);
            }
            if (gapRight != null && 0 == gapRight.gap && gapRight.direction == Orientation.SAME) {
                lanes.add(pright);
            }

            if (!lanes.isEmpty()) {
                Point dest = lanes.get(r.nextInt(lanes.size()));
                Misc.swapAgent(p, dest);
                return;
            }

            if (gapLeft != null && gapLeft.gap > 0 && gapLeft.direction == Orientation.SAME) {
                lanes.add(pleft);
            }
            if (gapRight != null && gapRight.gap > 0 && gapRight.direction == Orientation.SAME) {
                lanes.add(pright);
            }

            if (!lanes.isEmpty()) {
                Point dest = lanes.get(r.nextInt(lanes.size()));
                Misc.swapAgent(p, dest);
                return;
            }
        }

        // 2b-ii, 2b-iii
        if (gapMax == gapCenter.gap) {
            return;
        }

        // 2b-i
        List<Point> lanes = new ArrayList<Point>();
        if (gapLeft != null && gapMax == gapLeft.gap && getCellAssignment(board, pleft) == w) {
            lanes.add(pleft);
        }
        if (gapRight != null && gapMax == gapRight.gap && getCellAssignment(board, pright) == w) {
            lanes.add(pright);
        }

        // TODO (opcjonalnie): prawdopodobieństwo wyboru linii zależne od pola
        // potencjału

        if (!lanes.isEmpty()) {
            Point dest = lanes.get(r.nextInt(lanes.size()));
            Misc.swapAgent(p, dest);
        }
    }


    private GapReport calculateGap(Board board, MyPoint p, Agent w) {
        MyPoint currentCellCoords = new MyPoint(p);

        Orientation orientation = Orientation.SAME;

        // Luka na pasie przy założeniu, że nikt nie idzie nim z naprzeciwka.
        int gapSame = 2 * w.getvMax();

        // Luka na pasie przy założeniu, że znajduje się na nim przeciwnik.
        int gapOpp = w.getvMax();

        Agent opponent = null;

        for (int i = 1; i <= 2 * w.getvMax(); i++) {
            currentCellCoords = currentCellCoords.add(w.getDirection().getVec());

            if (!board.isOnBoard(currentCellCoords)) {
                if (i <= w.getvMax())
                    return new GapReport(Orientation.OUT, i, null);
                else
                    break;
            }

            if (!board.getCell(currentCellCoords).isPassable()) {
                gapSame = i - 1;
                break;
            }

            opponent = board.getCell(currentCellCoords).getAgent();
            if (opponent != null) {
                if (getRelativeOrientation(w.getDirection(), opponent.getDirection()) == Orientation.OPP) {
                    gapOpp = (i - 1) / 2;
                    orientation = Orientation.OPP;
                } else {
                    // ORTHO też traktowane jak SAME, bo bez ryzyka kolizji
                    gapSame = i - 1;
                }
                break;
            }
        }

        int gap = Collections.min(Arrays.asList(new Integer[] { w.getvMax(), gapSame, gapOpp }));
        return new GapReport(orientation, gap, opponent);
    }


    private void stepForward(Board board, Point cp, Map<Agent, Integer> mpLeft) {
        double p_exchg;

        Agent agent = board.getCell(cp).getAgent();
        MyPoint curr = new MyPoint(cp);

        if (mpLeft.get(agent) < 1)
            return;

        // (1)
        GapReport report = calculateGap(board, curr, agent);

        if (report.direction == Orientation.OUT && report.gap == 1) {
            mpLeft.put(agent, 0);
            Misc.setAgent(null, cp);
            return;
        }

        // (2) : "walka" o wspólne pole
        if (report.gap > 0) {
            MyPoint dest = curr.add(agent.getDirection().getVec());

            MyPoint[] points = new MyPoint[] { dest.add(agent.getDirection().nextCCW().getVec()),
                    dest.add(agent.getDirection().nextCW().getVec()) };

            for (MyPoint p : points) {
                if (board.isOnBoard(p) && board.getCell(p).isPassable()) {
                    Agent opponent = board.getCell(p).getAgent();
                    if (opponent != null) {
                        // Zobacz, czy faktycznie dochodzi do
                        // konfliktu...
                        if (p.add(opponent.getDirection().getVec()).equals(dest)) {
                            // konflikt
                            if (Math.random() < 0.5) {
                                Misc.swapAgent(curr, dest);
                                mpLeft.put(agent, mpLeft.get(agent) - 1);
                                agent.incrementFieldsMoved();
                            } else {
                                Misc.swapAgent(p, dest);
                                mpLeft.put(opponent, mpLeft.get(opponent) - 1);
                                opponent.incrementFieldsMoved();
                            }
                            return;
                        }
                    }
                }
            }

            // Brak konfliktu - zajmij pole.
            Misc.swapAgent(curr, dest);
            mpLeft.put(agent, mpLeft.get(agent) - 1);
            agent.incrementFieldsMoved();
        } else {

            // (3) : bi-directional
            if (report.direction == Orientation.OPP) {
                p_exchg = (agent.getAgility() + report.opponent.getAgility()) / 2;
                if (mpLeft.get(report.opponent) > 0 && Math.random() < p_exchg) {
                    MyPoint dest = curr.add(agent.getDirection().getVec());

                    // wyzeruj oryginalne pole oponenta
                    if (board.getCell(dest).getAgent() == null) {
                        MyPoint oppLoc = dest.add(agent.getDirection().getVec());
                        Misc.setAgent(null, oppLoc);
                    }

                    // board.swapAgent(curr, dest);
                    Misc.setAgent(agent, dest);
                    Misc.setAgent(report.opponent, curr);
                    mpLeft.put(agent, mpLeft.get(agent) - 1);
                    mpLeft.put(report.opponent, mpLeft.get(report.opponent) - 1);
                    agent.incrementFieldsMoved();
                    report.opponent.incrementFieldsMoved();
                    return;
                }
            }

            // (4) : bi-diagonal
            List<Point> l = new ArrayList<Point>();

            MyPoint[] points = new MyPoint[] {
                    curr.add(agent.getDirection().getVec()).add(agent.getDirection().nextCCW().getVec()),
                    curr.add(agent.getDirection().getVec()).add(agent.getDirection().nextCW().getVec()) };

            for (Point p : points) {
                if (board.isOnBoard(p) && board.getCell(p).isPassable()) {
                    Agent opponent = board.getCell(p).getAgent();
                    if (opponent != null
                            && getRelativeOrientation(agent.getDirection(), opponent.getDirection()) == Orientation.OPP
                            && mpLeft.get(opponent) > 0)
                        l.add(p);
                }
            }

            if (!l.isEmpty()) {
                Point dest = l.get(r.nextInt(l.size()));
                Agent t = board.getCell(dest).getAgent();
                p_exchg = (agent.getAgility() + t.getAgility()) / 2;

                if (Math.random() < p_exchg) {
                    Misc.setAgent(agent, dest);
                    Misc.setAgent(t, curr);
                    mpLeft.put(agent, mpLeft.get(agent) - 1);
                    mpLeft.put(t, mpLeft.get(t) - 1);
                    agent.incrementFieldsMoved();
                    t.incrementFieldsMoved();
                    return;
                }
            }

            // (5) : cross-diagonal
            MyPoint frontTile = curr.add(agent.getDirection().getVec());
            points = new MyPoint[] { frontTile.add(agent.getDirection().nextCCW().getVec()),
                    frontTile.add(agent.getDirection().nextCW().getVec()) };

            for (MyPoint p : points) {
                if (board.isOnBoard(p) && board.getCell(p).isPassable()) {
                    Agent opponent = board.getCell(p).getAgent();
                    if (opponent != null && p.add(opponent.getDirection().getVec()).equals(frontTile)
                            && mpLeft.get(opponent) > 0)
                        l.add(p);
                }
            }

            if (!l.isEmpty()) {
                Point dest = l.get(r.nextInt(l.size()));
                Agent t = board.getCell(dest).getAgent();
                p_exchg = (agent.getAgility() + t.getAgility()) / 2;

                if (Math.random() < p_exchg) {
                    Misc.setAgent(agent, dest);
                    Misc.setAgent(t, curr);
                    mpLeft.put(agent, mpLeft.get(agent) - 1);
                    mpLeft.put(t, mpLeft.get(t) - 1);
                    agent.incrementFieldsMoved();
                    t.incrementFieldsMoved();
                    return;
                }
            }

            // (6) : cross-forward-adjacent exchange
            frontTile = curr.add(agent.getDirection().getVec());

            if (board.isOnBoard(frontTile) && board.getCell(frontTile).isPassable()) {
                Agent opponent = board.getCell(frontTile).getAgent();
                p_exchg = (opponent != null) ? (agent.getAgility() + opponent.getAgility()) / 2 : 0;
                if (opponent != null
                        && getRelativeOrientation(agent.getDirection(), opponent.getDirection()) == Orientation.ORTHO
                        && mpLeft.get(opponent) > 0 && Math.random() < p_exchg) {
                    Misc.setAgent(agent, frontTile);
                    Misc.setAgent(opponent, curr);
                    mpLeft.put(agent, mpLeft.get(agent) - 1);
                    mpLeft.put(opponent, mpLeft.get(opponent) - 1);
                    agent.incrementFieldsMoved();
                    opponent.incrementFieldsMoved();
                    return;
                }
            }

            // Żadne z pól nie jest dostępne (same ściany): spróbuj obrócić się
            // i pójść w bok.
            MyPoint pleft = curr.add(agent.getDirection().nextCCW().getVec()).add(agent.getDirection().getVec());
            MyPoint pright = curr.add(agent.getDirection().nextCW().getVec()).add(agent.getDirection().getVec());
            MyPoint pcurr = curr.add(agent.getDirection().getVec());
            boolean gapLeft = (board.isOnBoard(pleft) && board.getCell(pleft).isPassable());
            boolean gapCenter = (board.isOnBoard(pcurr) && board.getCell(pcurr).isPassable());
            boolean gapRight = (board.isOnBoard(pright) && board.getCell(pright).isPassable());

            if (!gapLeft && !gapCenter && !gapRight) {
                // dostosuj kierunek

                // Trzeba pamiętać, że oś OY ma w programie przeciwny zwrot.
                double targetAngle = Math.toDegrees(Math.atan2(agent.getTarget().x - agent.getPosition().x,
                        agent.getPosition().y - agent.getTarget().y));

                MyPoint dirDest = agent.getPosition().add(agent.getDirection().getVec());
                double directionAngle = Math.toDegrees(Math.atan2(dirDest.x - agent.getPosition().x,
                        agent.getPosition().y - dirDest.y));

                if (targetAngle > directionAngle) {
                    agent.setDirection(agent.getDirection().nextCW());
                } else {
                    agent.setDirection(agent.getDirection().nextCCW());
                }
            }
        }
    }


    @Override
    public void nextIterationStep(Agent a, Map<Agent, Integer> mpLeft) {
        Board board = Mall.getInstance().getBoard();
        stepForward(board, a.getPosition(), mpLeft);
        long msecs = System.currentTimeMillis();
//        board.computeForceField();  // XXX: docelowo optymalniej
//        System.out.println(System.currentTimeMillis() - msecs);
    }


    @Override
    public void prepare(Agent a) {
        Board b = Mall.getInstance().getBoard();
        adjustDirection(a);
        
//        changeLane(b, a);
//        Mall.getInstance().getBoard().computeForceField();  // XXX: docelowo
                                                           // optymalniej
    }

}
