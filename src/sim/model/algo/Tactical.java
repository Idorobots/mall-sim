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

import sim.model.algo.MallFeature;
import sim.model.Agent;
import sim.model.Board;
import sim.model.Cell;

public class Tactical {
    public static final int HEURISTIC_FACTOR = 3;
    public static final int SCORE_FACTOR = 100;
    public static final int MAX_TARGETS = 15;
    public static final int MIN_TARGETS = 1;
    public static final int MIDPOINT_THRESHOLD = 75;
    public static final int SEGMENT_SIZE = 10;

    private boolean useMoore = true;
    private Random r;
    private Board board;


    public Tactical(Board board) {
        this.board = board;
        r = new Random();
    }


    public void useMooreNeighbourhood(boolean yn) {
        useMoore = yn;
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

        // Sort by distance to the Agent.
        //Collections.sort(targets, new PointComparator(agent.getPosition()));

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

            List<Point> neighbours = null;

            if(useMoore) {
                neighbours = getNeighboursMoore(current.point);
            }
            else {
                neighbours = getNeighboursVonNeumann(current.point);
            }

            for(Point neighbour : neighbours) {
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

        return selectMidpoints(allpoints);
    }


    private List<Point> selectMidpoints2(List<Point> points) {
        return points;
    }


    private List<Point> selectMidpoints(List<Point> points) {
        int size = points.size();

        if(points.size() > 1.5 * SEGMENT_SIZE) {
            List<Point> midpoints = new ArrayList<Point>();

            int numSegments = size / SEGMENT_SIZE;

            for(int i = 0; i < numSegments; ++i) {
                int s = i * SEGMENT_SIZE;
                int e = Math.min((i+1) * SEGMENT_SIZE - 1, size-1);

                midpoints.addAll(selectMidpoints(points, s, e));
            }

            return midpoints;
        }
        else {
            return points;
        }
    }


    private List<Point> selectMidpoints(List<Point> points, int start, int end) {
        List<Point> result = new ArrayList<Point>();

        if(end - start < 3) {
            result.add(points.get(start));
            result.add(points.get(end));
        }
        else {
            int middle = (start + end) / 2;
            int dot = dotProduct(points.get(start), points.get(middle), points.get(end));

            if(Math.abs(dot) < MIDPOINT_THRESHOLD) {
                List<Point> a = selectMidpoints(points, start, middle-1);
                List<Point> b = selectMidpoints(points, middle, end);

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


    private int dotProduct(Point a, Point b, Point c) {
        int Ax = (b.x - a.x);
        int Ay = (b.y - a.y);

        int Bx = (c.x - b.x);
        int By = (c.y - b.y);

        int dot = Ax * Bx + Ay * By;

        return dot;
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
        // TODO Take attractors into account.
        return SCORE_FACTOR;
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