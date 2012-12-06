package sim.model.algo;

import java.awt.Point;
import java.awt.Dimension;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Random;
import java.util.Comparator;
import java.util.Arrays;
import java.util.Collections;
import java.util.ListIterator;

import sim.util.Logger;

import sim.model.Agent;
import sim.model.Board;
import sim.model.Cell;

public class Tactical {
    public static final int MAX_TARGETS = 15;
    public static final int MIN_TARGETS = 1;
    public static final int MIDPOINT_INTERVAL = 10;
    public static final int MIDPOINT_THRESHOLD = 25;
    public static final int SEGMENT_SIZE = 20;

    private Random r;
    private Board board;


    public Tactical(Board board) {
        this.board = board;
        r = new Random();
    }


    public void innitializeTargets(Agent agent) {
        Logger.log(String.format("Initializing targets for %s...", agent));

        List<Point> targets = computeTargets(agent);

        Logger.log(String.format("Picked %d target positions.", targets.size()));
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


    public List<Point> computeTargets(Agent agent) {
        int numTargets = MIN_TARGETS + r.nextInt(MAX_TARGETS - MIN_TARGETS);
        Dimension dim = board.getDimension();

        ArrayList<Point> targets = new ArrayList<Point>();

        while(targets.size() < numTargets) {
            // TODO Select targets more sensibly.
            // TODO Take queues, holders and passages into account.
            Point p = new Point(r.nextInt(dim.width), r.nextInt(dim.height));

            if(board.getCell(p) != Cell.WALL) {
                targets.add(p);
            }
        }

        // Uses the stability of Collections.sort to our advantage.
        Collections.sort(targets, new PointComparator(true));
        Collections.sort(targets, new PointComparator(false));

        return targets;
    }


    public List<Point> computePath(Point start, Point target) {
        ArrayList<Node> closed = new ArrayList<Node>();
        ArrayList<Node> open = new ArrayList<Node>();

        open.add(new Node(start, 0, heuristicCostEstimate(start, target)));

        Node node = new Node(); // Only for lookups.
        Node current = null;
        NodeComparator nodeComparator = new NodeComparator();

        while(!open.isEmpty()) {
            Object[] arr = open.toArray();
            Arrays.sort(arr, nodeComparator);
            current = (Node) arr[0];

            open.remove(current);
            closed.add(current);

            if(current.point.equals(target)) {
                break;
            }

            for(Point neighbour : getNeighbours(current.point)) {
                node.point = neighbour;

                if(closed.contains(node)) {
                    continue;
                }

                int score = current.score + getScoreDelta(current.point, neighbour);

                if(open.contains(node)) {
                    if(score <= node.score) {
                        Node n = open.get(open.indexOf(node));

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

        int size = allpoints.size();
        if(allpoints.size() > 1.5 * SEGMENT_SIZE) {
            List<Point> midpoints = new ArrayList<Point>();

            int numSegments = size / SEGMENT_SIZE;

            for(int i = 0; i < numSegments; ++i) {
                int s = i * SEGMENT_SIZE;
                int e = Math.min((i+1) * SEGMENT_SIZE - 1, size-1);
                int m = (i * SEGMENT_SIZE + e) / 2;

                midpoints.addAll(selectMidpoints(allpoints, s, m, e));
            }

            return midpoints;
        }
        else {
            return allpoints;
        }
    }

    private List<Point> selectMidpoints(List<Point> points, int start, int middle, int end) {
        // XXX
        // System.out.println(String.format("%d: %d, %d, %d", points.size(), start, middle, end));

        List<Point> result = new ArrayList<Point>();

        if(end - start < 4) {
            result.add(points.get(start));
            result.add(points.get(middle));
            result.add(points.get(middle));
        }
        else {
            int dot = dotProduct(points.get(start), points.get(middle),
                                 points.get(middle), points.get(end));

            if(Math.abs(dot) < MIDPOINT_THRESHOLD) {

                int m1 = (start + middle) / 2;
                int m2 = (middle + end) / 2;

                List<Point> a = selectMidpoints(points, start, m1, middle-1);
                List<Point> b = selectMidpoints(points, middle, m2, end);

                result.addAll(a);
                result.addAll(b);
            }
            else {
                result.add(points.get(start));
                result.add(points.get(middle));
                result.add(points.get(end));
            }
        }
        return result;
    }

    private int dotProduct(Point a, Point b, Point c, Point d) {
        int Ax = (b.x - a.x);
        int Ay = (b.y - a.y);

        int Bx = (d.x - c.x);
        int By = (d.y - c.y);

        int dot = Ax * Bx + Ay * By;

        // XXX
        // System.out.println(dot);

        return dot;
    }

    private int heuristicCostEstimate(Point p, Point target) {
        // TODO Take atractors into account.
        return (int) p.distanceSq(target);
    }


    private List<Point> getNeighbours(Point point) {
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
        // TODO Take attractors into account.
        return 1;
    }


    private String pathToString(List<Point> path) {
        String s = "";

        for(Point p : path) {
            s += String.format("(%d, %d) -> ", p.x, p.y);
        }

        return s;
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
        private boolean xy = true;

        public PointComparator(boolean xy) {
            this.xy = xy;
        }

        public int compare(Object a, Object b) {
            Point pa = (Point) a;
            Point pb = (Point) b;

            if(xy) {
                return pa.x - pb.x;
            }
            else {
                return pa.y - pb.y;
            }
        }

        public boolean equals(Object o) {
            if(o instanceof NodeComparator) {
                return this == o;
            }
            return false;
        }
    }
}