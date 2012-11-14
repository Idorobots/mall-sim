package sim.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import sim.gui.actions.ExitAction;
import sim.model.Mall;

@SuppressWarnings("serial")
public class MallFrame extends JFrame {

    private JPanel contentPane;
    private GUIBoard guiBoard;

    /**
     * Create the frame.
     */
    public MallFrame(Mall mall) {
        setTitle("MallSim");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 650, 500);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu mnFile = new JMenu("File");
        mnFile.setMnemonic('F');
        menuBar.add(mnFile);

        mnFile.add(new ExitAction());

        JMenu mnSimulation = new JMenu("Simulation");
        mnSimulation.setMnemonic('S');
        menuBar.add(mnSimulation);

        JMenuItem mntmLoadMall = new JMenuItem("Load mall...");
        mnSimulation.add(mntmLoadMall);

        JMenu mnHelp = new JMenu("Help");
        mnHelp.setMnemonic('H');
        menuBar.add(mnHelp);

        JMenuItem mntmAbout = new JMenuItem("About");
        mnHelp.add(mntmAbout);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));

        JPanel statusBar = new JPanel();
        statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        contentPane.add(statusBar, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(1.0);
        contentPane.add(splitPane, BorderLayout.CENTER);
        splitPane.setDividerLocation(this.getBounds().width / 2);  // XXX: temp
//         splitPane.setDividerLocation(0.8); // XXX: docelowa wersja

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        splitPane.setRightComponent(tabbedPane);

        JPanel panel = new JPanel();
        tabbedPane.addTab("Properties", null, panel, null);

        JTextArea txtrTuIdWszelkie = new JTextArea();
        txtrTuIdWszelkie.setEditable(false);
        txtrTuIdWszelkie.setRows(5);
        txtrTuIdWszelkie
                .setText("--- INFO ---\r\nTu idą wszelkie wyświetlane\r\nwłaściwości aktorów, atraktorów,\r\npłytek itp.");
        panel.add(txtrTuIdWszelkie);

        JScrollPane boardScrollPane = new JScrollPane();
        boardScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        boardScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        splitPane.setLeftComponent(boardScrollPane);

        JPanel boardPanel = new JPanel();
        boardScrollPane.setViewportView(boardPanel);

        guiBoard = new GUIBoard(mall.getBoard());
        boardPanel.add(guiBoard);
    }

}
