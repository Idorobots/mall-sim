package sim;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import sim.gui.MallFrame;
import sim.model.Agent;
import sim.model.Board;
import sim.model.Cell;
import sim.model.Mall;
import sim.model.helpers.Direction;

import sim.control.ResourceManager;

public class MallSim {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {

			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				// ResourceManager resMgr = new ResourceManager();

				Mall mall = new Mall();
				test(mall.getBoard());

				MallFrame frame = new MallFrame(mall);
				frame.setVisible(true);

				SimLoop loop = new SimLoop(mall);
				loop.addObserver(frame.getBoard());
				Thread t = new Thread(loop);
				t.start();
			}
		});
	}

	private static class SimLoop extends Observable implements Runnable {

		private Mall mall;

		public SimLoop(Mall mall) {
			super();
			this.mall = mall;
		}

		@Override
		public void run() {

			int nAgents = 0;
			for (int y = 0; y < mall.getBoard().getDimension().height; y++) {
				for (int x = 0; x < mall.getBoard().getDimension().width; x++) {
					if (mall.getBoard().getCell(new Point(x, y)).getAgent() != null)
						nAgents++;
				}
			}

			for (int i = 0; i < 10; i++) {

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}

				// mall.getBoard().print();

				// Movement
				for (int y = 0; y < mall.getBoard().getDimension().height; y++) {
					for (int x = 0; x < mall.getBoard().getDimension().width; x++) {
						Point p = new Point(x, y);
						Cell c = mall.getBoard().getCell(p);

						if (c.getAgent() != null)
							c.getAlgorithm().prepare(mall.getBoard(), p);
					}
				}

				this.setChanged();
				this.notifyObservers();

				Map<Agent, Integer> speedPointsLeft = new WeakHashMap<Agent, Integer>();

				for (int y = 0; y < mall.getBoard().getDimension().height; y++) {
					for (int x = 0; x < mall.getBoard().getDimension().width; x++) {
						Point p = new Point(x, y);
						Agent a = mall.getBoard().getCell(p).getAgent();

						if (a != null)
							speedPointsLeft.put(a, a.getvMax());
					}
				}

				for (int step = 0; step < Agent.V_MAX; step++) {
					Set<Agent> moved = new HashSet<Agent>();
					for (int y = 0; y < mall.getBoard().getDimension().height; y++) {
						for (int x = 0; x < mall.getBoard().getDimension().width; x++) {
							Point p = new Point(x, y);
							Agent a = mall.getBoard().getCell(p).getAgent();

							if (a == null || moved.contains(a))
								continue;

							moved.add(a);

							if (speedPointsLeft.get(a) > 0) {
								speedPointsLeft.put(a,
										speedPointsLeft.get(a) - 1);
								mall.getBoard()
										.getCell(p)
										.getAlgorithm()
										.nextIterationStep(mall.getBoard(), p,
												speedPointsLeft);
							}
						}
					}
//					System.out.println(moved.size());
					assert (moved.size() == nAgents);
				}

				this.setChanged();
				this.notifyObservers();

				// mall.getBoard().print();

			}
		}

	}

	private static void test(Board b) {
		final int N_AGENTS = 20;
		Random r = new Random();
		Dimension d = b.getDimension();

		// Point p = new Point((int) d.getWidth() / 2, (int) d.getHeight() / 2);
		//
		// b.getCell(p).setAgent(new Agent());
		// b.getCell(new Point(p.x, p.y + 1)).setAgent(new Agent());

		for (int i = 0; i < N_AGENTS; i++) {
			Cell c = b.getCell(new Point(r.nextInt(d.width), r
					.nextInt(d.height)));

			if (c != Cell.WALL) {
				Agent a = new Agent();
				a.setDirection(Direction.values()[r.nextInt(Direction.values().length)]);
				c.setAgent(a);
			}
		}

	}
}
