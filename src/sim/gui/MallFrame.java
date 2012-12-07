package sim.gui;

import java.awt.BorderLayout;

import javax.swing.ButtonGroup;
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

import sim.control.GuiState;
import sim.control.Listeners;
import sim.control.GuiState.DrawTargetLinePolicy;
import sim.gui.actions.ExitAction;
import sim.model.Mall;
import javax.swing.JCheckBox;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.border.TitledBorder;
import javax.swing.JRadioButton;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

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
        // splitPane.setDividerLocation(0.8); // XXX: docelowa wersja

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setSelectedIndex(-1);
        splitPane.setRightComponent(tabbedPane);

        JPanel tabDisplay = new JPanel();
        tabbedPane.addTab("Display", null, tabDisplay, null);
        GridBagLayout gbl_tabDisplay = new GridBagLayout();
        gbl_tabDisplay.columnWidths = new int[] { 306, 0 };
        gbl_tabDisplay.rowHeights = new int[] { 0, 205, 0 };
        gbl_tabDisplay.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
        gbl_tabDisplay.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
        tabDisplay.setLayout(gbl_tabDisplay);

        JCheckBox chckbxShowSocialForce = new JCheckBox("show social force field");
        chckbxShowSocialForce.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                JCheckBox cb = (JCheckBox) arg0.getSource();
                guiBoard.setShowForceField(cb.isSelected());
            }
        });
        GridBagConstraints gbc_chckbxShowSocialForce = new GridBagConstraints();
        gbc_chckbxShowSocialForce.fill = GridBagConstraints.BOTH;
        gbc_chckbxShowSocialForce.insets = new Insets(0, 0, 5, 0);
        gbc_chckbxShowSocialForce.gridx = 0;
        gbc_chckbxShowSocialForce.gridy = 0;
        tabDisplay.add(chckbxShowSocialForce, gbc_chckbxShowSocialForce);

        ButtonGroup targetLinesGroup = new ButtonGroup();

        JPanel targetVectorsPanel = new JPanel();
        targetVectorsPanel.setBorder(new TitledBorder(null, "Target vectors", TitledBorder.LEADING, TitledBorder.TOP,
                null, null));
        GridBagConstraints gbc_targetVectorsPanel = new GridBagConstraints();
        gbc_targetVectorsPanel.fill = GridBagConstraints.BOTH;
        gbc_targetVectorsPanel.gridx = 0;
        gbc_targetVectorsPanel.gridy = 1;
        tabDisplay.add(targetVectorsPanel, gbc_targetVectorsPanel);
        targetVectorsPanel.setLayout(new BoxLayout(targetVectorsPanel, BoxLayout.Y_AXIS));

        JRadioButton rdbtnNone = new JRadioButton("none");
        rdbtnNone.setActionCommand(DrawTargetLinePolicy.NONE.name());
        rdbtnNone.addActionListener(Listeners.targetLineListener);
        targetVectorsPanel.add(rdbtnNone);

        JRadioButton rdbtnSelection = new JRadioButton("selection");
        rdbtnSelection.setActionCommand(DrawTargetLinePolicy.SELECTION.name());
        rdbtnSelection.addActionListener(Listeners.targetLineListener);
        targetVectorsPanel.add(rdbtnSelection);

        JRadioButton rdbtnAll = new JRadioButton("all");
        rdbtnAll.setActionCommand(DrawTargetLinePolicy.ALL.name());
        rdbtnAll.addActionListener(Listeners.targetLineListener);
        targetVectorsPanel.add(rdbtnAll);
        targetLinesGroup.add(rdbtnNone);
        targetLinesGroup.add(rdbtnSelection);
        targetLinesGroup.add(rdbtnAll);

        switch (GuiState.targetLinePolicy) {
        case NONE:
            rdbtnNone.doClick();
        case SELECTION:
            rdbtnNone.doClick();
        case ALL:
            rdbtnNone.doClick();
        }

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


    public GUIBoard getBoard() {
        return guiBoard;
    }
}
