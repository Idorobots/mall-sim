package sim.model.algo;

import java.awt.Point;
import java.awt.Dimension;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Random;
import java.util.Comparator;
import java.util.Collections;

import sim.util.Logger;

import sim.model.Agent;
import sim.model.Board;
import sim.model.Cell;

public class Tactical {
    public static final int MAX_TARGETS = 10;
    public static final int MIN_TARGETS = 1;
    public static final int MIDPOINT_INTERVAL = 10;

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

        LinkedList<Point> targets = new LinkedList<Point>();

        while(targets.size() < numTargets) {
            // TODO Select targets more sensibly.
            // TODO Take queues, holders and passages into account.
            Point p = new Point(r.nextInt(dim.width), r.nextInt(dim.height));

            if(board.getCell(p) != Cell.WALL) {
                targets.add(p);
            }
        }

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
            Collections.sort(open, nodeComparator);

            current = open.get(0);

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
                    Node n = open.get(open.indexOf(node));
                    if(score <= node.score) {
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

        List<Point> midpoints = new ArrayList<Point>();

        int skip = 0;
        while(current != null) {
            if(skip == 0) {
                midpoints.add(0, current.point);
                skip = MIDPOINT_INTERVAL;
            }
            skip--;
            current = current.prev;
        }

        return midpoints;
    }


    private int heuristicCostEstimate(Point p, Point target) {
        return (int) p.distanceSq(target);
    }


    private List<Point> getNeighbours(Point point) {
        int x = point.x;
        int y = point.y;

        Dimension dim = board.getDimension();

        ArrayList<Point> neighbours = new ArrayList<Point>();

        for(int j = y-1; j <= y+1 ; ++j) {
            for(int i = x-1; i <= x+1; ++i) {
                if(x == i && y == j)         continue;
                if(i < 0 || i >= dim.width)  continue;
                if(j < 0 || j >= dim.height) continue;

                Point p = new Point(i, j);

                if(board.getCell(p) != Cell.WALL) {
                    neighbours.add(p);
                }
            }
        }

        return neighbours;
    }


    private int getScoreDelta(Point a, Point b) {
        return (int) a.distance(b);
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
}