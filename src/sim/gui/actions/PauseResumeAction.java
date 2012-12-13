package sim.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import sim.MallSim;

@SuppressWarnings("serial")
public class PauseResumeAction extends AbstractAction {

    public PauseResumeAction() {
        super("Paused");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                KeyEvent.VK_P, ActionEvent.ALT_MASK));
    }


    @Override
    public void actionPerformed(ActionEvent e) {
		MallSim.setThreadState(!MallSim.getThreadState());
    }
}