package sim.model.algo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import sim.model.Agent;
import sim.model.Board;
import sim.model.Cell;
import sim.model.helpers.Direction;
import sim.model.helpers.MyPoint;

public final class Ped4 extends MovementAlgorithm {

    /**
     * Klasa służy do zwracania informacji z funkcji calculateGap().
     * 
     * @author Pawel
     * 
     */
    static class GapReport {
        public final Dir direction;
        public int gap;
        public final Agent opponent;

        public GapReport(Dir direction, int gap, Agent opponent) {
            super();
            this.direction = direction;
            this.gap = gap;
            this.opponent = opponent;
        }

    }

    public Ped4(Board board) {
        super(board);
    }

    // bezwzględnie maksymalna ilość pól możliwych do przejścia w jednej
    // iteracji
    public static final int V_MAX = 6;

    // TODO: jak rozwiązać problem różnych v_max dla różnych ludzi?
    private static Random r = new Random();

    enum Lane {
        LEFT, SAME, RIGHT
    }

    enum Dir {
        SAME, OPP, ORTHO, OUT
    }

    private static Agent[][] temp_lattice;
    private static Agent[][] assignedTo;

    private void init() {
        assignedTo = new Agent[board.getDimension().height][board
                .getDimension().width];
        temp_lattice = new Agent[board.getDimension().height][board
                .getDimension().width];

        for (int i = 0; i < board.getDimension().height; i++)
            for (int j = 0; j < board.getDimension().width; j++) {
                temp_lattice[i][j] = null;
                assignedTo[i][j] = null;
            }
    }

    private Dir getDir(Direction dir1, Direction dir2) {
        if (dir1 == dir2)
            return Dir.SAME;

        return (Math.abs(dir1.diff(dir2)) == 1) ? Dir.ORTHO : Dir.OPP;
    }

    private void _changeLane() {

        // Determine tile availability.
        for (int h = 0; h < board.getDimension().height; h++) {
            for (int w = 0; w < board.getDimension().width; w++) {
                List<Agent> candidates = new ArrayList<Agent>();
                Point p = null;
                Point current = new Point(w, h);

                // Wybierz tylko pieszych, których kierunek ruchu dopuszcza
                // zejśce w bok na dane pole.
                if (board.getCell(current).getAgent() == null) {
                    p = new Point(w - 1, h);
                    if (board.isOnBoard(p)
                            && board.getCell(p).getAgent() != null
                            && board.getCell(p).getAgent().getDirection()
                                    .isVertical())
                        candidates.add(board.getCell(p).getAgent());

                    p = new Point(w + 1, h);
                    if (board.isOnBoard(p)
                            && board.getCell(p).getAgent() != null
                            && board.getCell(p).getAgent().getDirection()
                                    .isVertical())
                        candidates.add(board.getCell(p).getAgent());

                    p = new Point(w, h - 1);
                    if (board.isOnBoard(p)
                            && board.getCell(p).getAgent() != null
                            && board.getCell(p).getAgent().getDirection()
                                    .isHorizontal())
                        candidates.add(board.getCell(p).getAgent());

                    p = new Point(w, h + 1);
                    if (board.isOnBoard(p)
                            && board.getCell(p).getAgent() != null
                            && board.getCell(p).getAgent().getDirection()
                                    .isHorizontal())
                        candidates.add(board.getCell(p).getAgent());
                }

                assignedTo[h][w] = (candidates.size() == 0) ? null : candidates
                        .get(r.nextInt(candidates.size()));
                temp_lattice[h][w] = null;
            }
        }

        for (int h = 0; h < board.getDimension().height; h++) {
            for (int w = 0; w < board.getDimension().width; w++) {
                Point current = new Point(w, h);
                Agent walk = board.getCell(current).getAgent();
                if (walk != null)
                    changeLane(new MyPoint(current), walk);
            }
        }

        for (int h = 0; h < board.getDimension().height; h++) {
            for (int w = 0; w < board.getDimension().width; w++) {
                board.getCell(new Point(w, h)).setAgent(temp_lattice[h][w]);
            }
        }
    }

    private void changeLane(MyPoint p, Agent w) {

        // TODO: problem - alejka w ogóle nie istnieje na planszy (pleft not on
        // board), czy wyjście powoduje

        // FIXME: czy obrót jest dobrze zrobiony? - nie
        MyPoint pleft = p.add(w.getDirection().rotateLeft().getCoords());
        MyPoint pright = p.add(w.getDirection().rotateRight().getCoords());
        GapReport gapLeft = board.isOnBoard(pleft) ? calculateGap(pleft, w)
                : null;
        GapReport gapCenter = calculateGap(p, w);
        GapReport gapRight = board.isOnBoard(pright) ? calculateGap(pright, w)
                : null;

        if (gapLeft == null && gapRight == null)
            return;
        // if (gapLeft != null && gapLeft.get(0) == Dir.OUT || gapCenter.get(0)
        // == Dir.OUT || gapRight != null
        // && gapRight.get(0) == Dir.OUT) {
        // System.out.println("outside");
        // return;
        // }

        int gapMax = gapCenter.gap;
        if (gapLeft != null) {
            gapMax = Math.max(gapMax, gapLeft.gap);
        }
        if (gapRight != null) {
            gapMax = Math.max(gapMax, gapRight.gap);
        }

        // 2a-i
        if (gapCenter.direction == Dir.OPP) {
            gapCenter.gap = 0;

            // 2a-ii
            List<Point> lanes = new ArrayList<Point>();
            if (gapLeft != null && 0 == gapLeft.gap
                    && gapLeft.direction == Dir.SAME) {
                lanes.add(pleft);
            }
            if (gapRight != null && 0 == gapRight.gap
                    && gapRight.direction == Dir.SAME) {
                lanes.add(pright);
            }

            if (!lanes.isEmpty()) {
                Point dest = lanes.get(r.nextInt(lanes.size()));
                temp_lattice[dest.y][dest.x] = w;
                return;
            }
        }

        // 2b-ii, 2b-iii
        if (gapMax == gapCenter.gap) {
            temp_lattice[p.y][p.x] = w;
            return;
        }

        // 2b-i
        List<Point> lanes = new ArrayList<Point>();
        if (gapLeft != null && gapMax == (Integer) gapLeft.gap
                && assignedTo[pleft.y][pleft.x] == w) {
            lanes.add(pleft);
        }
        if (gapRight != null && gapMax == (Integer) gapRight.gap
                && assignedTo[pright.y][pright.x] == w) {
            lanes.add(pright);
        }

        if (!lanes.isEmpty()) {
            Point dest = lanes.get(r.nextInt(lanes.size()));
            temp_lattice[dest.y][dest.x] = w;
        } else {
            temp_lattice[p.y][p.x] = w;
        }
    }

    private GapReport calculateGap(MyPoint p, Agent w) {
        MyPoint np = new MyPoint(p);

        Dir dir = Dir.SAME;
        int gap_same = 2 * w.getvMax();
        int gap_opp = w.getvMax();
        Agent op = null;

        for (int i = 1; i <= 2 * w.getvMax(); i++) {
            np = np.add(w.getDirection().getCoords());
            if (!board.isOnBoard(np)) {
                if (i <= w.getvMax())
                    return new GapReport(Dir.OUT, i, null);
                else
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

    private void stepForward() {
        final double p_exchg = 0.5;

        // lista już poruszonych osób
        List<Agent> moved = new Vector<Agent>();
        List<MyPoint> toMove = new Vector<MyPoint>();

        main: for (int h = 0; h < board.getDimension().height; h++) {
            for (int w = 0; w < board.getDimension().width; w++) {
                MyPoint curr = new MyPoint(w, h);
                Cell cell = board.getCell(curr);

                Agent walk = cell.getAgent();
                if (walk == null || moved.contains(walk))
                    continue;

                cell.setAgent(null);

                // (1)
                GapReport report = calculateGap(curr, walk);
                walk.setvCurr(report.gap);

                if (report.direction == Dir.OUT) {
                    cell.setAgent(walk);
                    toMove.add(curr);
                    continue;
                }

                // (2) : "walka" o wspólne pole
                whole: while (walk.getvCurr() > 0) {
                    MyPoint dest = curr.add(walk.getDirection().getCoords()
                            .mul(walk.getvCurr()));
                    walk.setvCurr(walk.getvCurr() - 1); // Sprawdź obie strony
                                                        // pola...
                    for (int i = 1; i <= V_MAX; i++) {
                        MyPoint[] points = new MyPoint[] {
                                dest.add(walk.getDirection().rotateLeft()
                                        .getCoords().mul(i)),
                                dest.add(walk.getDirection().rotateRight()
                                        .getCoords().mul(i)) };

                        for (MyPoint p : points) {
                            if (board.isOnBoard(p)) {
                                Agent walk_opp = board.getCell(p).getAgent();
                                if (walk_opp != null) {
                                    // Zobacz, czy faktycznie dochodzi do
                                    // konfliktu...
                                    GapReport gapOpp = calculateGap(p, walk_opp);
                                    if (p.add(
                                            walk_opp.getDirection().getCoords()
                                                    .mul(gapOpp.gap)).equals(
                                            dest)) {
                                        // konflikt
                                        if (Math.random() < 0.5) {
                                            board.getCell(dest).setAgent(walk);
                                            moved.add(walk);
                                            continue main;
                                        } else {
                                            board.getCell(dest).setAgent(
                                                    walk_opp);
                                            board.getCell(p).setAgent(null);
                                            moved.add(walk_opp);
                                            continue whole;
                                        }
                                    }
                                }
                            }
                        }
                    } // Brak konfliktu - zajmij pole.
                    board.getCell(dest).setAgent(walk);
                    moved.add(walk);
                    continue main;
                }

                // (3) : bi-directional
                if (walk.getvCurr() == 0 && report.direction == Dir.OPP) {
                    if (!moved.contains(report.opponent)
                            && Math.random() < p_exchg) {
                        MyPoint dest = curr
                                .add(walk.getDirection().getCoords());

                        // wyzeruj oryginalne pole oponenta
                        if (board.getCell(dest).getAgent() == null) {
                            MyPoint oppLoc = dest.add(walk.getDirection()
                                    .getCoords());
                            board.getCell(oppLoc).setAgent(null);
                        }

                        board.getCell(dest).setAgent(walk);
                        board.getCell(curr).setAgent(report.opponent);
                        moved.add(walk);
                        moved.add(report.opponent);
                        continue;
                    }
                }

                // (4) : bi-diagonal
                if (walk.getvCurr() == 0) {
                    List<Point> l = new ArrayList<Point>();

                    Point[] points = new Point[] {
                            curr.add(walk.getDirection().getCoords()).add(
                                    walk.getDirection().rotateLeft()
                                            .getCoords()),
                            curr.add(walk.getDirection().getCoords()).add(
                                    walk.getDirection().rotateRight()
                                            .getCoords()) };

                    for (Point p : points) {
                        if (board.isOnBoard(p)) {
                            Agent walk_opp = board.getCell(p).getAgent();
                            if (walk_opp != null
                                    && getDir(walk.getDirection(),
                                            walk_opp.getDirection()) == Dir.OPP
                                    && !moved.contains(walk_opp))
                                l.add(p);
                        }
                    }

                    if (!l.isEmpty() && Math.random() < p_exchg) {
                        Point dest = l.get(r.nextInt(l.size()));
                        Agent t = board.getCell(dest).getAgent();
                        board.getCell(dest).setAgent(walk);
                        board.getCell(curr).setAgent(t);
                        moved.add(walk);
                        moved.add(t);
                        continue;
                    }
                }

                // (5) : cross-diagonal
                if (walk.getvCurr() == 0) {
                    List<Point> l = new ArrayList<Point>();

                    MyPoint frontTile = curr.add(walk.getDirection()
                            .getCoords());
                    MyPoint[] points = new MyPoint[] {
                            frontTile.add(walk.getDirection().rotateLeft()
                                    .getCoords()),
                            frontTile.add(walk.getDirection().rotateRight()
                                    .getCoords()) };

                    for (MyPoint p : points) {
                        if (board.isOnBoard(p)) {
                            Agent walk_opp = board.getCell(p).getAgent();
                            if (walk_opp != null
                                    && p.add(
                                            walk_opp.getDirection().getCoords())
                                            .equals(frontTile)
                                    && !moved.contains(walk_opp))
                                l.add(p);
                        }
                    }

                    if (!l.isEmpty() && Math.random() < p_exchg) {
                        Point dest = l.get(r.nextInt(l.size()));
                        Agent t = board.getCell(dest).getAgent();
                        board.getCell(dest).setAgent(walk);
                        board.getCell(curr).setAgent(t);
                        moved.add(walk);
                        moved.add(t);
                        continue;
                    }
                }

                // (6) : cross-forward-adjacent exchange
                if (walk.getvCurr() == 0) {
                    Point frontTile = curr.add(walk.getDirection().getCoords());

                    if (board.isOnBoard(frontTile)) {
                        Agent walk_opp = board.getCell(frontTile).getAgent();
                        if (walk_opp != null
                                && getDir(walk.getDirection(),
                                        walk_opp.getDirection()) == Dir.ORTHO
                                && !moved.contains(walk_opp)
                                && Math.random() < p_exchg) {
                            board.getCell(frontTile).setAgent(walk);
                            board.getCell(curr).setAgent(walk_opp);
                            moved.add(walk);
                            moved.add(walk_opp);
                            continue;
                        }
                    }
                }

                board.getCell(curr).setAgent(walk);
                toMove.add(curr);
            }
        }

        for (MyPoint p : toMove) {
            Agent walk = board.getCell(p).getAgent();
            board.getCell(p).setAgent(null);
            MyPoint dest = p.add(walk.getDirection().getCoords()
                    .mul(walk.getvCurr()));

            if (board.isOnBoard(dest)) {
                board.getCell(dest).setAgent(walk);
                moved.add(walk);
            } else {
                System.out.println(String.format("[%d, %d] outside", p.y, p.x));
            }
        }
    }

    @Override
    public void nextIterationStep() {
        init();
        _changeLane();
        stepForward();
    }

}
