package sim.model.algo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import sim.model.Agent;
import sim.model.Board;
import sim.model.algo.Ped4.GapReport;
import sim.model.helpers.Direction;
import sim.model.helpers.MyPoint;

/* 
 * FIXME: blokowanie, gdy zbyt silne pole: osoba krąży na granicy pola
 *      FIX: przebyty dystans podczas osiągania celu wpływa na postrzeganie pola potencjału (długi spacer sprawia, że osoby chętniej przechodzą blisko innych)
 */

/**
 * (1) Aktualizacja rozkładu pola potencjału.
 * (2) Wybór pola o najwyższym potencjale.
 * (3) Dostosowanie kierunku ruchu agenta do nowego pola.
 * 
 * Pole o najwyższym potencjale wybierane jest spośród dostępnych pól. Dostępne
 * pola to te, które znajdują się w zasięgu kierunku celu i kierunku ruchu.
 * 
 * @author Pawel Kleczek
 * 
 */
public class SocialForce implements MovementAlgorithm {

    private static MovementAlgorithm instance = new SocialForce();

    private static Random r = new Random();


    private SocialForce() {
    }


    public static MovementAlgorithm getInstance() {
        return instance;
    }


    @Override
    public void prepare(Board board, Point p) {
        // Social Force nie potrzebuje fazy wstępnej.
        return;
    }


    @Override
    public void nextIterationStep(Board board, Point p, Map<Agent, Integer> mpLeft) {
        board.modifyForceField(p, -1);

        Point hpt = getHighestPotentialTile(board, p);

        Agent a = board.getCell(p).getAgent();
        // Brak możliwości ruchu - agent "drepcze" w miejscu.
        if (hpt == null) {
            a.setDirection(a.getDirection().rotateRight());
            return;
        }

        adjustDirection(a, p, hpt);
        board.swapAgent(p, hpt);

        a.incrementFieldsMoved();

        board.modifyForceField(hpt, 1);
    }


    private void adjustDirection(Agent a, Point prev, Point curr) {
        if (a.getDirection() == Direction.N || a.getDirection() == Direction.S) {
            if (curr.y == prev.y) {
                if (curr.x < prev.x)
                    a.setDirection(Direction.W);
                else
                    a.setDirection(Direction.E);
            }
        } else {
            if (curr.x == prev.x) {
                if (curr.y < prev.y)
                    a.setDirection(Direction.N);
                else
                    a.setDirection(Direction.S);
            }
        }
    }


    /**
     * 
     * @param b
     * @param agentPosition
     * @return <code>null</code> gdy nie ma możliwości ruchu
     */
    private Point getHighestPotentialTile(Board b, Point agentPosition) {

        List<Point> points = getAvailableTiles(b, agentPosition);

        // Brak możliwości ruchu - pozostań w miejscu.
        if (points.isEmpty())
            return null;

        Agent a = b.getCell(agentPosition).getAgent();

        // Przy obliczaniu wartości potencjału uwzględniamy potencjał celu.
        Map<Point, Double> potentialMap = new HashMap<Point, Double>();
        Point closestToTarget = null;
        double minDistToTarget = Double.MAX_VALUE;
        Point target = a.getTarget();
        for (Point p : points) {
            // TODO: modyfikacja wzoru?
            double targetForce = 1 / target.distanceSq(p);
            potentialMap.put(p, b.getCell(p).getForceValue() + targetForce);

            double dist = p.distance(target);
            if (dist < minDistToTarget)
                closestToTarget = p;
        }

        // Agent jest sfrustrowany chodzeniem - idzie po najmniejszej linii
        // oporu.
        final double MIN_WALKING_FORCE = 1.3;
        double walkingForce = a.getFieldsMoved() / a.getInitialDistanceToTarget();
        // XXX: zostawić?
        // if (walkingForce > MIN_WALKING_FORCE)
        // potentialMap.put(closestToTarget, potentialMap.get(closestToTarget) +
        // walkingForce);

        // Pozostaw pola o najwyższym potencjale.
        double maxPotential = Collections.max(potentialMap.values());
        List<Point> highestPoints = new ArrayList<Point>();
        for (Map.Entry<Point, Double> entry : potentialMap.entrySet()) {
            if (entry.getValue() == maxPotential)
                highestPoints.add(entry.getKey());
        }

        // Wybierz płytkę najbliżej celu.
        List<Point> closestPoints = new ArrayList<Point>();
        double distMin = Double.MAX_VALUE;
        target = new MyPoint(b.getCell(agentPosition).getAgent().getTarget());
        for (Point p : highestPoints) {
            distMin = Math.min(distMin, target.distance(p));
        }

        for (Point p : highestPoints) {
            if (distMin == target.distance(p))
                closestPoints.add(p);
        }

        return closestPoints.get(r.nextInt(closestPoints.size()));
    }


    /**
     * 
     * @param a
     *            agent
     * @param p
     *            pozycja agenta
     * @return
     */
    private List<Point> getAvailableTiles(Board b, Point p) {

        List<Point> points;
        ;

        Agent a = b.getCell(p).getAgent();
        Direction d = getTargetDirection(a.getTarget(), p);

        // Punkty dopuszczalne ze względu na kierunek do celu.
        List<Point> targetPoints = new ArrayList<Point>();
        MyPoint front = d.getCoords().add(p);
        MyPoint left = d.rotateLeft().getCoords().add(p);
        MyPoint right = d.rotateRight().getCoords().add(p);

        if (testCell(b, left))
            targetPoints.add(left);
        if (testCell(b, front))
            targetPoints.add(front);
        if (testCell(b, right))
            targetPoints.add(right);

        // Punkty dopuszczalne ze względu na aktualny kierunek ruchu.
        List<Point> agentPoints = new ArrayList<Point>();
        front = a.getDirection().getCoords().add(p);
        left = a.getDirection().rotateLeft().getCoords().add(p);
        right = a.getDirection().rotateRight().getCoords().add(p);

        if (testCell(b, left))
            agentPoints.add(left);
        if (testCell(b, front))
            agentPoints.add(front);
        if (testCell(b, right))
            agentPoints.add(right);

        points = new ArrayList<Point>(targetPoints);
        points.retainAll(agentPoints);

        // INFO: na chwilę obecną nie dopuszczamy ruchu na skos
        // if (testCell(b, front) && testCell(b, left))
        // points.add(left.add(d.getCoords()));
        // if (testCell(b, front) && testCell(b, right))
        // points.add(right.add(d.getCoords()));

        // assert (!points.isEmpty());

        return points;
    }


    private boolean testCell(Board b, Point p) {
        return (b.isOnBoard(p) && b.getCell(p).getAgent() == null && b.getCell(p).isPassable());
    }


    private Direction getTargetDirection(Point target, Point position) {
        // Trzeba pamiętać, że oś OY ma w programie przeciwny zwrot.
        double targetAngle = Math.toDegrees(Math.atan2(target.x - position.x, position.y - target.y));
        if (targetAngle < 0.0)
            targetAngle += 360.0;
        assert (targetAngle >= 0.0d && targetAngle <= 360.0d);

        if (targetAngle < 45.0d)
            return Direction.N;
        else if (targetAngle < 135.0d)
            return Direction.E;
        else if (targetAngle < 210.0d)
            return Direction.S;
        else if (targetAngle < 300.0d)
            return Direction.W;
        else
            return Direction.N;
    }


    private Dir getDir(Direction dir1, Direction dir2) {
        if (dir1 == dir2)
            return Dir.SAME;

        return (Math.abs(dir1.diff(dir2)) == 1) ? Dir.ORTHO : Dir.OPP;
    }


    private void stepForward(Board board, Point cp, Map<Agent, Integer> mpLeft) {
        final double p_exchg = 0.5;

        Agent walk = board.getCell(cp).getAgent();
        MyPoint curr = new MyPoint(cp);

        if (mpLeft.get(walk) < 1)
            return;

        // (1)
        GapReport report = calculateGap(board, curr, walk);

        // (3) : bi-directional
        if (report.direction == Dir.OPP) {
            if (mpLeft.get(report.opponent) > 0 && Math.random() < p_exchg) {
                MyPoint dest = curr.add(walk.getDirection().getCoords());

                // wyzeruj oryginalne pole oponenta
                if (board.getCell(dest).getAgent() == null) {
                    MyPoint oppLoc = dest.add(walk.getDirection().getCoords());
                    board.getCell(oppLoc).setAgent(null);
                }

                // board.swapAgent(curr, dest);
                board.getCell(dest).setAgent(walk);
                board.getCell(curr).setAgent(report.opponent);
                mpLeft.put(walk, mpLeft.get(walk) - 1);
                mpLeft.put(report.opponent, mpLeft.get(report.opponent) - 1);
                return;
            }
        }

        // (4) : bi-diagonal
        List<Point> l = new ArrayList<Point>();

        MyPoint[] points = new MyPoint[] {
                curr.add(walk.getDirection().getCoords()).add(walk.getDirection().rotateLeft().getCoords()),
                curr.add(walk.getDirection().getCoords()).add(walk.getDirection().rotateRight().getCoords()) };

        for (Point p : points) {
            if (board.isOnBoard(p) && board.getCell(p).isPassable()) {
                Agent walk_opp = board.getCell(p).getAgent();
                if (walk_opp != null && getDir(walk.getDirection(), walk_opp.getDirection()) == Dir.OPP
                        && mpLeft.get(walk_opp) > 0)
                    l.add(p);
            }
        }

        if (!l.isEmpty() && Math.random() < p_exchg) {
            Point dest = l.get(r.nextInt(l.size()));
            Agent t = board.getCell(dest).getAgent();
            board.getCell(dest).setAgent(walk);
            board.getCell(curr).setAgent(t);
            mpLeft.put(walk, mpLeft.get(walk) - 1);
            mpLeft.put(t, mpLeft.get(t) - 1);
            return;
        }

        // (5) : cross-diagonal
        MyPoint frontTile = curr.add(walk.getDirection().getCoords());
        points = new MyPoint[] { frontTile.add(walk.getDirection().rotateLeft().getCoords()),
                frontTile.add(walk.getDirection().rotateRight().getCoords()) };

        for (MyPoint p : points) {
            if (board.isOnBoard(p) && board.getCell(p).isPassable()) {
                Agent walk_opp = board.getCell(p).getAgent();
                if (walk_opp != null && p.add(walk_opp.getDirection().getCoords()).equals(frontTile)
                        && mpLeft.get(walk_opp) > 0)
                    l.add(p);
            }
        }

        if (!l.isEmpty() && Math.random() < p_exchg) {
            Point dest = l.get(r.nextInt(l.size()));
            Agent t = board.getCell(dest).getAgent();
            board.getCell(dest).setAgent(walk);
            board.getCell(curr).setAgent(t);
            mpLeft.put(walk, mpLeft.get(walk) - 1);
            mpLeft.put(t, mpLeft.get(t) - 1);
            return;
        }

        // (6) : cross-forward-adjacent exchange
        frontTile = curr.add(walk.getDirection().getCoords());

        if (board.isOnBoard(frontTile) && board.getCell(frontTile).isPassable()) {
            Agent walk_opp = board.getCell(frontTile).getAgent();
            if (walk_opp != null && getDir(walk.getDirection(), walk_opp.getDirection()) == Dir.ORTHO
                    && mpLeft.get(walk_opp) > 0 && Math.random() < p_exchg) {
                board.getCell(frontTile).setAgent(walk);
                board.getCell(curr).setAgent(walk_opp);
                mpLeft.put(walk, mpLeft.get(walk) - 1);
                mpLeft.put(walk_opp, mpLeft.get(walk_opp) - 1);
                return;
            }
        }
    }


    private GapReport calculateGap(Board board, MyPoint p, Agent w) {
        MyPoint np = new MyPoint(p);

        Dir dir = Dir.SAME;
        int gap_same = 2 * w.getvMax();
        int gap_opp = w.getvMax();
        Agent op = null;

        for (int i = 1; i <= 1; i++) {
            np = np.add(w.getDirection().getCoords());
            if (!board.isOnBoard(np)) {
                if (i <= w.getvMax())
                    return new GapReport(Dir.OUT, i, null);
                else
                    break;
            }

            if (!board.getCell(np).isPassable()) {
                gap_same = i - 1;
                break;
            }

            op = board.getCell(np).getAgent();
            if (op != null) {
                if (getDir(w.getDirection(), op.getDirection()) == Dir.OPP) {
                    gap_opp = (i - 1) / 2;
                    dir = Dir.OPP;
                } else {
                    // ORTHO też traktowane jak SAME, bo bez ryzyka kolizji
                    gap_same = i - 1;
                }
                break;
            }
        }

        int gap = Math.min(w.getvMax(), Math.min(gap_same, gap_opp));

        return new GapReport(dir, gap, op);
    }
}
