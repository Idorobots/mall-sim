package sim.model.algo;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import sim.MallSim;
import sim.model.Agent;
import sim.model.Board;
import sim.model.Cell;
import sim.util.Logger;

public class Tactical {
    public static final int HEURISTIC_FACTOR = 5;
    public static final int SCORE_FACTOR = 100;
    public static final int MAX_TARGETS = 15;
    public static final int MIN_TARGETS = 1;
    public static final int MAX_SEGMENT_SIZE = 7;
    public static final int PATH_SMOOTHING = 3;

    private boolean useMoore = true;
    private Random r;
    private Board board;


    public Tactical(Board board) {
        this.board = board;
        r = MallSim.r;
    }


    public void useMooreNeighbourhood(boolean yn) {
        useMoore = yn;
    }


    public void initializeTargets(Agent agent) {
        int numTargets = MIN_TARGETS + r.nextInt(MAX_TARGETS - MIN_TARGETS);

        initializeTargets(agent, numTargets);
    }


    public void initializeTargets(Agent agent, int numTargets) {
        Logger.log(String.format("Initializing targets for %s...", agent));

        Point[] targets = computeTargets(agent, numTargets);

        Logger.log(String.format("Picked %d target positions.", targets.length));
        Logger.log("Generating paths...");

        Point last = agent.getPosition();
        for(Point target : targets) {
            List<Point> midpoints = computePath(last, target);

            for(Point midpoint : midpoints) {
                agent.addTarget(midpoint);
            }

            last = target;
        }

        Logger.log("Paths generated!");
        Logger.log("Targets initialized!");
    }


    public Point[] computeTargets(Agent agent, int numTargets) {
        Dimension dim = board.getDimension();

        ArrayList<Point> targets = new ArrayList<Point>();

        while(targets.size() < numTargets) {
            Point p = new Point(r.nextInt(dim.width), r.nextInt(dim.height));

            if(board.getCell(p) != Cell.WALL) {
                targets.add(p);
            }
        }

        // Sort by overal distance:
        Point[] targetArray = new Point[targets.size()];
        targets.toArray(targetArray);

        Arrays.sort(targetArray, new PointComparator(agent.getPosition()));

        int size = targetArray.length;

        for(int i = 1; i < size; ++i) {
            Arrays.sort(targetArray, i, size, new PointComparator(targetArray[i-1]));
        }

        return targetArray;
    }


    public List<Point> computePath(Point start, Point target) {
        // NOTE O(N log H(target)) :(

        int width = board.getDimension().width;
        int height = board.getDimension().height;

        BitSet closed = new BitSet(width * height);
        PriorityQueue<Node> open = new PriorityQueue<Node>(100, new NodeComparator());

        open.add(new Node(start, 0, heuristicCostEstimate(start, target)));

        Node node = new Node(); // Only for lookups.
        Node current = null;

        while(!open.isEmpty()) {
            current = open.poll();

            Point c = current.point;
            closed.set(c.y * width + c.x);

            if(c.equals(target)) {
                break;
            }

            List<Point> neighbours = null;

            if(useMoore) {
                neighbours = getNeighboursMoore(c);
            }
            else {
                neighbours = getNeighboursVonNeumann(c);
            }

            for(Point neighbour : neighbours) {
                node.point = neighbour;

                if(closed.get(neighbour.y * width + neighbour.x)) {
                    continue;
                }

                int score = current.score + getScoreDelta(c, neighbour);

                if(open.contains(node)) {
                    if(score <= node.score) {
                        Node n = null;

                        Iterator<Node> iter = open.iterator();

                        while(iter.hasNext()) {
                            n = iter.next();

                            if(n.equals(node)) break;
                        }

                        assert n != null; // Improbable, yet so, so scary...

                        n.prev = current;
                        n.score = score;
                        n.estimate = score + heuristicCostEstimate(neighbour, target);
                    }
                }
                else {
                    Node n = new Node(neighbour);

                    n.prev = current;
                    n.score = score;
                    n.estimate = score + heuristicCostEstimate(neighbour, target);

                    open.add(n);
                }
            }
        }

        List<Point> allpoints = new ArrayList<Point>();

        if(current == null || !current.point.equals(target)) {
            return allpoints; // Nowhere to go.
        }

        while(current != null) {
            allpoints.add(0, current.point);
            current = current.prev;
        }

        return selectMidpoints(allpoints);
    }

    private List<Point> selectMidpoints(List<Point> points) {
        int size = points.size();

        if(size > MAX_SEGMENT_SIZE) {
            List<Point> midpoints = new ArrayList<Point>();

            int numSegments = size / MAX_SEGMENT_SIZE;

            for(int i = 0; i < numSegments; ++i) {
                int s = i * MAX_SEGMENT_SIZE;
                int e = Math.min((i+1) * MAX_SEGMENT_SIZE, size-1);

                List<Point> segment = selectMidpoints(points, s, e);

                // NOTE For the first segment its starting point is the Agent position,
                // NOTE which is not a part of the path.
                // NOTE For all other segments their starting points are the same as the ending
                // NOTE points of the previous segments, so we can safely remove them.

                segment.remove(0);
                midpoints.addAll(segment);
            }

            return midpoints;
        }
        else {
            return selectMidpoints(points, 0, size-1);
        }
    }

    private List<Point> selectMidpoints(List<Point> points, int start, int end) {
        // NOTE O(N log N) :(

        List<Point> result = new ArrayList<Point>();

        Point s = points.get(start);
        Point e = points.get(end);

        if(end == start) {
            result.add(s);
        }
        if(end - start < PATH_SMOOTHING) {
            for(int i = start; i <= end; ++i) {
                result.add(points.get(i));
            }
        }
        else if(lineClear(s, e)) {
            result.add(s);
            result.add(e);
        }
        else {
            int middle = (start + end) / 2;

            List<Point> a = selectMidpoints(points, start, middle);
            List<Point> b = selectMidpoints(points, middle, end);
            b.remove(0); // NOTE We don't want midpoint duplication.

            result.addAll(a);
            result.addAll(b);
        }

        return result;
    }


    private boolean lineClear(Point a, Point b) {
        // NOTE O(N) :(

        if(a.equals(b)) return true;

        Point iter = new Point(a);

        double x = a.x;
        double y = a.y;
        double dx = b.x - a.x;
        double dy = b.y - a.y;

        double len = Math.sqrt(dx * dx + dy * dy);

        dx /= len;
        dy /= len;

        while(!iter.equals(b)) {
            iter.x = (int) x;
            iter.y = (int) y;

            if(board.getCell(iter) == Cell.WALL) return false;

            x += dx;
            y += dy;
        }

        return true;
    }


    private int heuristicCostEstimate(Point p, Point target) {
        int score = (int) (SCORE_FACTOR * HEURISTIC_FACTOR *  p.distance(target));

        MallFeature mf = board.getCell(p).getFeature();

        if(mf != null) {
            return mf.modifyHeuristicEstimate(score);
        }

        return score;
    }


    private List<Point> getNeighboursVonNeumann(Point point) {
        int x = point.x;
        int y = point.y;

        Dimension dim = board.getDimension();
        int w = dim.width;
        int h = dim.height;

        Point p = new Point(); // For lookups.

        ArrayList<Point> neighbours = new ArrayList<Point>();

        for(int j = -1; j <= 1 ; ++j) {
            for(int i = -1; i <= 1 ; ++i) {
                if(Math.abs(i) == Math.abs(j)) continue;

                p.x = x+i;
                p.y = y+j;

                if(p.x < 0) continue;
                if(p.x >= w) continue;
                if(p.y < 0) continue;
                if(p.y >= h) continue;

                if(board.getCell(p) != Cell.WALL) {
                    neighbours.add(new Point(p.x, p.y));
                }
            }
        }

        return neighbours;
    }


    private List<Point> getNeighboursMoore(Point point) {
        int x = point.x;
        int y = point.y;

        Dimension dim = board.getDimension();
        int w = dim.width;
        int h = dim.height;

        Point p = new Point(); // For lookups.

        ArrayList<Point> neighbours = new ArrayList<Point>();

        for(int j = y-1; j <= y+1 ; ++j) {
            for(int i = x-1; i <= x+1; ++i) {
                if(x == i && y == j) continue;
                if(i < 0 || i >= w)  continue;
                if(j < 0 || j >= h)  continue;

                p.x = i;
                p.y = j;

                if(board.getCell(p) != Cell.WALL) {
                    neighbours.add(new Point(i, j));
                }
            }
        }

        return neighbours;
    }


    private int getScoreDelta(Point a, Point b) {
        return SCORE_FACTOR;
    }


    private static class Node {
        public Point point = null;
        public int score = 0;
        public int estimate = 0;
        public Node prev = null;

        public Node() {
            this(null);
        }

        public Node(Point point) {
            this(point, 0, 0);
        }

        public Node(Point point, int score, int estimate) {
            this.point = point;
            this.score = score;
            this.estimate = estimate;
        }

        public boolean equals(Object o) {
            if(o instanceof Node) {
                Node n = (Node) o;
                return this.point.equals(n.point);
            }

            return false;
        }
    }


    private static class NodeComparator implements Comparator {
        public int compare(Object a, Object b) {
            Node na = (Node) a;
            Node nb = (Node) b;

            return na.estimate - nb.estimate;
        }

        public boolean equals(Object o) {
            if(o instanceof NodeComparator) {
                return this == o;
            }
            return false;
        }
    }

    private static class PointComparator implements Comparator {
        private Point p = null;

        public PointComparator(Point p) {
            this.p = p;
        }

        public int compare(Object a, Object b) {
            Point pa = (Point) a;
            Point pb = (Point) b;

            if(pa.distance(pb) < 20) return 0;

            return (int) (p.distanceSq(pa) - p.distanceSq(pb));
        }

        public boolean equals(Object o) {
            if(o instanceof NodeComparator) {
                return this == o;
            }
            return false;
        }
    }
}