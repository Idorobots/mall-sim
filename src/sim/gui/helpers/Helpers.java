package sim.gui.helpers;

public class Helpers {
    public static int clamp (int i, int low, int high) {
        return Math.max(Math.min(i, high), low);
    }
}