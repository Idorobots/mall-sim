package sim.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import sim.control.GuiState.BackgroundPolicy;
import sim.control.GuiState.DrawTargetLinePolicy;

public abstract class Listeners {

    public static final ActionListener targetLineListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            GuiState.targetLinePolicy = DrawTargetLinePolicy.valueOf(e.getActionCommand());
        }
    };

    public static final ActionListener backgroundListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            GuiState.backgroundPolicy = BackgroundPolicy.valueOf(e.getActionCommand());
        }
    };
}
