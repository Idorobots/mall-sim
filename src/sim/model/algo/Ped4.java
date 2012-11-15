package sim.model.algo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import sim.model.Agent;
import sim.model.Board;
import sim.model.helpers.Direction;
import sim.model.helpers.MyPoint;

public final class Ped4 implements MovementAlgorithm {

	private static MovementAlgorithm instance = new Ped4();

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

	private Ped4() {
	}

	public static MovementAlgorithm getInstance() {
		return instance;
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

	private Dir getDir(Direction dir1, Direction dir2) {
		if (dir1 == dir2)
			return Dir.SAME;

		return (Math.abs(dir1.diff(dir2)) == 1) ? Dir.ORTHO : Dir.OPP;
	}

	private Agent getCellAssignment(Board board, Point cellCoord) {
		List<Agent> candidates = new ArrayList<Agent>();
		Point p = null;

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

		return (candidates.size() == 0) ? null : candidates.get(r
				.nextInt(candidates.size()));
	}

	private void changeLane(Board board, MyPoint p) {
		Agent w = board.getCell(p).getAgent();

		assert (w != null);

		// TODO: problem - alejka w ogóle nie istnieje na planszy (pleft not on
		// board), czy wyjście powoduje

		MyPoint pleft = p.add(w.getDirection().rotateLeft().getCoords());
		MyPoint pright = p.add(w.getDirection().rotateRight().getCoords());
		GapReport gapLeft = board.isOnBoard(pleft) ? calculateGap(board, pleft,
				w) : null;
		GapReport gapCenter = calculateGap(board, p, w);
		GapReport gapRight = board.isOnBoard(pright) ? calculateGap(board,
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
				board.getCell(p).setAgent(null);
				board.getCell(dest).setAgent(w);
				return;
			}
		}

		// 2b-ii, 2b-iii
		if (gapMax == gapCenter.gap) {
			return;
		}

		// 2b-i
		List<Point> lanes = new ArrayList<Point>();
		if (gapLeft != null && gapMax == gapLeft.gap
				&& getCellAssignment(board, pleft) == w) {
			lanes.add(pleft);
		}
		if (gapRight != null && gapMax == gapRight.gap
				&& getCellAssignment(board, pright) == w) {
			lanes.add(pright);
		}

		if (!lanes.isEmpty()) {
			Point dest = lanes.get(r.nextInt(lanes.size()));
			board.getCell(p).setAgent(null);
			board.getCell(dest).setAgent(w);
		} else {
		}
	}

	private GapReport calculateGap(Board board, MyPoint p, Agent w) {
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

	// FIXME: bywa, że piesi sprawiają wrażenie, jakby na siebie wpadali
	private void stepForward(Board board, Point cp, Map<Agent, Integer> mpLeft) {
		final double p_exchg = 0.5;

		Agent walk = board.getCell(cp).getAgent();
		MyPoint curr = new MyPoint(cp);

		if (mpLeft.get(walk) < 1)
			return;

		// (1)
		GapReport report = calculateGap(board, curr, walk);

		if (report.direction == Dir.OUT && report.gap == 1) {
			mpLeft.put(walk, 0);
			// XXX: temp
//			board.getCell(cp).setAgent(null);
			return;
		}

		// (2) : "walka" o wspólne pole
		if (report.gap > 0) {
			MyPoint dest = curr.add(walk.getDirection().getCoords());

			MyPoint[] points = new MyPoint[] {
					dest.add(walk.getDirection().rotateLeft().getCoords()),
					dest.add(walk.getDirection().rotateRight().getCoords()) };

			for (MyPoint p : points) {
				if (board.isOnBoard(p)) {
					Agent walk_opp = board.getCell(p).getAgent();
					if (walk_opp != null) {
						// Zobacz, czy faktycznie dochodzi do
						// konfliktu...
						if (p.add(walk_opp.getDirection().getCoords()).equals(
								dest)) {
							// konflikt
							if (Math.random() < 0.5) {
								board.getCell(curr).setAgent(null);
								board.getCell(dest).setAgent(walk);
							} else {
								board.getCell(dest).setAgent(walk_opp);
								board.getCell(p).setAgent(null);
							}

							mpLeft.put(walk, mpLeft.get(walk) - 1);
							mpLeft.put(walk_opp, mpLeft.get(walk_opp) - 1);
						}
					}
				}
			}

			// Brak konfliktu - zajmij pole.
			board.getCell(curr).setAgent(null);
			board.getCell(dest).setAgent(walk);
			mpLeft.put(walk, mpLeft.get(walk) - 1);
		} else {

			// (3) : bi-directional
			if (report.direction == Dir.OPP) {
				if (mpLeft.get(report.opponent) > 0 && Math.random() < p_exchg) {
					MyPoint dest = curr.add(walk.getDirection().getCoords());

					// wyzeruj oryginalne pole oponenta
					// XXX: czy to potrzebne?
					if (board.getCell(dest).getAgent() == null) {
						MyPoint oppLoc = dest.add(walk.getDirection()
								.getCoords());
						board.getCell(oppLoc).setAgent(null);
					}

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
					curr.add(walk.getDirection().getCoords()).add(
							walk.getDirection().rotateLeft().getCoords()),
					curr.add(walk.getDirection().getCoords()).add(
							walk.getDirection().rotateRight().getCoords()) };

			for (Point p : points) {
				if (board.isOnBoard(p)) {
					Agent walk_opp = board.getCell(p).getAgent();
					if (walk_opp != null
							&& getDir(walk.getDirection(),
									walk_opp.getDirection()) == Dir.OPP
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
			points = new MyPoint[] {
					frontTile.add(walk.getDirection().rotateLeft().getCoords()),
					frontTile
							.add(walk.getDirection().rotateRight().getCoords()) };

			for (MyPoint p : points) {
				if (board.isOnBoard(p)) {
					Agent walk_opp = board.getCell(p).getAgent();
					if (walk_opp != null
							&& p.add(walk_opp.getDirection().getCoords())
									.equals(frontTile)
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

			if (board.isOnBoard(frontTile)) {
				Agent walk_opp = board.getCell(frontTile).getAgent();
				if (walk_opp != null
						&& getDir(walk.getDirection(), walk_opp.getDirection()) == Dir.ORTHO
						&& mpLeft.get(walk_opp) > 0 && Math.random() < p_exchg) {
					board.getCell(frontTile).setAgent(walk);
					board.getCell(curr).setAgent(walk_opp);
					mpLeft.put(walk, mpLeft.get(walk) - 1);
					mpLeft.put(walk_opp, mpLeft.get(walk_opp) - 1);
					return;
				}
			}
		}
	}

	@Override
	public void nextIterationStep(Board board, Point p,
			Map<Agent, Integer> mpLeft) {
		stepForward(board, p, mpLeft);
	}

	@Override
	public void prepare(Board board, Point p) {
		changeLane(board, new MyPoint(p));
	}

}
