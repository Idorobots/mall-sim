package sim.model.algo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import sim.MallSim;
import sim.model.Agent;
import sim.model.Board;
import sim.model.Mall;
import sim.model.helpers.Direction;
import sim.model.helpers.Misc;
import sim.model.helpers.MyPoint;

/* 
 * FIXME: blokowanie, gdy zbyt silne pole: osoba krąży na granicy pola
 *      FIX: przebyty dystans podczas osiągania celu wpływa na postrzeganie pola potencjału (długi spacer sprawia, że osoby chętniej przechodzą blisko innych)
 */

/**
 * (1) Aktualizacja rozkładu pola potencjału. (2) Wybór pola o najwyższym
 * potencjale. (3) Dostosowanie kierunku ruchu agenta do nowego pola.
 * 
 * Pole o najwyższym potencjale wybierane jest spośród dostępnych pól. Dostępne
 * pola to te, które znajdują się w zasięgu kierunku celu i kierunku ruchu.
 * 
 * @author Pawel Kleczek
 * 
 */
public class SocialForce implements MovementAlgorithm {

	private static MovementAlgorithm instance = new SocialForce();

	private static Random r = MallSim.r;

	private SocialForce() {
	}

	public static MovementAlgorithm getInstance() {
		return instance;
	}

	@Override
	public void prepare(Agent a) {
		// Social Force nie potrzebuje fazy wstępnej.
		return;
	}

	@Override
	public void nextIterationStep(Agent a, Map<Agent, Integer> mpLeft) {
		final double EXCHANGE_CHANCE = 0.5;
		Board board = Mall.getInstance().getBoard();

		Point hpt = getHighestPotentialTile(board, a.getPosition());

		// Brak możliwości ruchu - agent "drepcze" w miejscu.
		if (hpt == null || hpt != null && board.getCell(hpt).getAgent() != null
				&& Math.random() < EXCHANGE_CHANCE) {
			a.setDirection(a.getDirection().nextCW());
		} else {
			MyPoint p = a.getPosition();
			Misc.swapAgent(p, hpt);
			adjustDirection(a, p);
			a.incrementFieldsMoved();
		}
	}

	/**
	 * Dostosowuje kierunek dalszego marszu do wykonanego ruchu.
	 * 
	 * @param a
	 * @param prev
	 */
	private void adjustDirection(Agent a, Point prev) {
		if (a.getDirection() == Direction.N || a.getDirection() == Direction.S) {
			if (a.getPosition().y == prev.y) {
				if (a.getPosition().x < prev.x)
					a.setDirection(Direction.W);
				else
					a.setDirection(Direction.E);
			}
		} else {
			if (a.getPosition().x == prev.x) {
				if (a.getPosition().y < prev.y)
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

		List<Point> points = getAvailableTiles(b, new MyPoint(agentPosition));

		// Brak możliwości ruchu - pozostań w miejscu.
		if (points.isEmpty())
			return null;

		Agent a = b.getCell(agentPosition).getAgent();

		// Przy obliczaniu wartości potencjału uwzględniamy potencjał celu.
		Map<Point, Double> potentialMap = new HashMap<Point, Double>();
		Point target = a.getTarget();
		for (Point p : points) {
			// TODO: kalibracja
			final double targetForce = 5 / target.distance(p);
			final double agentPenalty = (b.getCell(p).getAgent() == null) ? 0.0
					: -10.0;
			potentialMap.put(p, b.getCell(p).getForceValue() + targetForce
					+ agentPenalty);
		}

		// Agent jest sfrustrowany chodzeniem, przez co mniej zważa na innych
		// ludzi.
		final double MIN_WALKING_FORCE = 1.3;
		double distToTarget = a.getInitialDistanceToTarget();
		double walkingForce = (distToTarget > 0.0) ? a.getFieldsMoved()
				/ distToTarget : 0;

		if (walkingForce > MIN_WALKING_FORCE) {
			final double WALKING_FORCE_MODIFIER = 3.0;

			// Get point closest to target.
			Point closestToTarget = null;
			double minDistToTarget = Double.MAX_VALUE;
			for (Point p : points) {
				double dist = p.distance(target);
				if (dist < minDistToTarget) {
					closestToTarget = p;
					minDistToTarget = dist;
				}
			}

			potentialMap.put(closestToTarget, potentialMap.get(closestToTarget)
					+ walkingForce * WALKING_FORCE_MODIFIER);
		}

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
	 * @param board
	 *            plansza
	 * @param agentPosition
	 *            pozycja agenta
	 * @return
	 */
	private List<Point> getAvailableTiles(Board board, MyPoint agentPosition) {

		List<Point> points;

		Agent a = board.getCell(agentPosition).getAgent();
		Direction d = getTargetDirection(a.getTarget(), agentPosition);

		// Punkty dopuszczalne ze względu na kierunek do celu.
		List<Point> targetPoints = new ArrayList<Point>();
		MyPoint front = agentPosition.add(d.getVec());
		MyPoint left = agentPosition.add(d.nextCCW().getVec());
		MyPoint right = agentPosition.add(d.nextCW().getVec());

		if (isCellAccessible(board, left))
			targetPoints.add(left);
		if (isCellAccessible(board, front))
			targetPoints.add(front);
		if (isCellAccessible(board, right))
			targetPoints.add(right);

		// Punkty dopuszczalne ze względu na aktualny kierunek ruchu.
		List<Point> agentPoints = new ArrayList<Point>();
		front = agentPosition.add(a.getDirection().getVec());
		left = agentPosition.add(a.getDirection().nextCCW().getVec());
		right = agentPosition.add(a.getDirection().nextCW().getVec());

		if (isCellAccessible(board, left))
			agentPoints.add(left);
		if (isCellAccessible(board, front))
			agentPoints.add(front);
		if (isCellAccessible(board, right))
			agentPoints.add(right);

		points = new ArrayList<Point>(targetPoints);
		points.retainAll(agentPoints);

		return points;
	}

	private boolean isCellAccessible(Board b, Point p) {
		return (b.isOnBoard(p) && b.getCell(p).isPassable());
	}

	/**
	 * Zwraca kierunek celu (kierunek świata wg najmniejszego odchylenia od
	 * kierunku wektora celu).
	 * 
	 * @param target
	 * @param position
	 * @return
	 */
	private Direction getTargetDirection(Point target, Point position) {
		// Trzeba pamiętać, że oś OY ma w programie przeciwny zwrot.
		double targetAngle = Math.toDegrees(Math.atan2(target.x - position.x,
				position.y - target.y));
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
}
