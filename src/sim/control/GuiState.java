package sim.control;

import sim.model.Agent;

public class GuiState {

    public static enum DrawTargetLinePolicy {
        NONE, SELECTION, SELECTION_ROUTE, ALL
    }

    private static Agent selectedAgent = null;

    public static DrawTargetLinePolicy targetLinePolicy = DrawTargetLinePolicy.NONE;


    public static Agent getSelectedAgent() {
        return selectedAgent;
    }


    public static void setSelectedAgent(Agent selectedAgent) {
        GuiState.selectedAgent = selectedAgent;

        // TODO: uaktualnić kontrolki
    }

}
