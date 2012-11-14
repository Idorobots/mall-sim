package mallsim;

import java.awt.EventQueue;

import javax.swing.UIManager;

import mallsim.gui.MallFrame;

import sim.model.Mall;

public class MallSim {

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                try {
                	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                	Mall mall = new Mall();
                    MallFrame frame = new MallFrame(mall.getBoard());
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
