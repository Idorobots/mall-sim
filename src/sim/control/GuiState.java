package sim.control;

import java.awt.Point;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import sim.gui.MallFrame;
import sim.model.Agent;
import sim.model.helpers.Direction;
import sim.model.helpers.MyPoint;
import sim.model.helpers.Vec;

public class GuiState {

    public static enum DrawTargetLinePolicy {
        NONE, SELECTION, SELECTION_ROUTE, ALL
    }

    public static enum BackgroundPolicy {
        NONE, SOCIAL_FIELD, VISITS, FEATURES, MOVEMENT_ALGORITHM
    }

    private static Agent selectedAgent = null;

    public static DrawTargetLinePolicy targetLinePolicy = DrawTargetLinePolicy.NONE;
    public static BackgroundPolicy backgroundPolicy = BackgroundPolicy.NONE;

    public static int animationSpeed = 300;


    public static Agent getSelectedAgent() {
        return selectedAgent;
    }


    public static void setSelectedAgent(Agent selectedAgent, MallFrame frame) {

        // TODO: uaktualnić kontrolki
        if (selectedAgent == null && GuiState.selectedAgent != null)
            GuiState.selectedAgent.deleteObservers();
        
        GuiState.selectedAgent = selectedAgent;

        if (selectedAgent != null)
            GuiState.selectedAgent.addObserver(frame.getPropertiesTable());
    }

}
