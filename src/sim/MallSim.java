package sim;

import java.awt.EventQueue;

import javax.swing.UIManager;

import sim.gui.MallFrame;

import sim.control.ResourceManager;

public class MallSim {

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    ResourceManager resMgr = new ResourceManager();

                    MallFrame frame = new MallFrame(resMgr.loadShoppingMall("./data/malls/1floor.bmp"));
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
