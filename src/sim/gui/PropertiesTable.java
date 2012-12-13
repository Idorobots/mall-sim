package sim.gui;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import sim.control.GuiState;
import sim.model.Agent;

@SuppressWarnings("serial")
public class PropertiesTable extends JTable implements Observer {

    public PropertiesTable() {
        updateAgentData(null);
    }


    public void updateAgentData(Agent agent) {

        Object[][] data = null;

        if (agent == null) {
            Object[][] d = { { "vMax", "" }, { "agility", "" }, { "position", "" }, { "direction", "" },
                    { "route length", "" }, { "fieldsMoved", "" }, { "initialDistanceToTarget", "" },
                    { "holdTime", "" } };
            data = d;
        } else {

            String position = String.format("[%d, %d]", agent.getPosition().x, agent.getPosition().y);

            Object[][] d = { { "vMax", agent.getvMax() }, { "agility", agent.getAgility() }, { "position", position },
                    { "direction", agent.getDirection().name() }, { "route length", agent.getRoute().size() },
                    { "fieldsMoved", agent.getFieldsMoved() },
                    { "initialDistanceToTarget", agent.getInitialDistanceToTarget() },
                    { "holdTime", agent.getHoldTime() } };
            data = d;
        }

        setModel(new DefaultTableModel(data, new String[] { "Parameter", "Value" }));

    }


    @Override
    public void update(Observable o, Object arg) {
        updateAgentData(GuiState.getSelectedAgent());
    }

}
