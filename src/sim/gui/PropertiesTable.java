package sim.gui;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import sim.control.GuiState;
import sim.model.Agent;

@SuppressWarnings("serial")
public class PropertiesTable extends JTable implements Observer {

    private static Object[][] rawData = { { "vMax", "" }, { "agility", "" }, { "position", "" }, { "direction", "" },
            { "route length", "" }, { "fieldsMoved", "" }, { "initialDistanceToTarget", "" },
            { "holdTime", "" } };
    
    private static String[] columnNames = { "Parameter", "Value" };
    
    public PropertiesTable() {
        super(rawData, columnNames);
        
        updateAgentData(null);
    }


    public void updateAgentData(Agent agent) {

        TableModel m = getModel();
        
        if (agent == null) {
            for (int i = 0; i < rawData.length; i++)
                m.setValueAt("", i, 1);
        } else {

            String position = String.format("[%d, %d]", agent.getPosition().x, agent.getPosition().y);

            m.setValueAt(agent.getvMax(), 0, 1);
            m.setValueAt(agent.getAgility(), 1, 1);
            m.setValueAt(position, 2, 1);
            m.setValueAt(agent.getDirection().name(), 3, 1);
            m.setValueAt(agent.getRoute().size(), 4, 1);
            m.setValueAt(agent.getFieldsMoved(), 5, 1);
            m.setValueAt(agent.getInitialDistanceToTarget(), 6, 1);
            m.setValueAt(agent.getHoldTime(), 7, 1);
        }
    }


    @Override
    public void update(Observable o, Object arg) {
        updateAgentData(GuiState.getSelectedAgent());
    }

}
