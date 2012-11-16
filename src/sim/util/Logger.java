package sim.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logger class for logging.
 */

public class Logger {
    public static void log(String what) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");

        System.out.println(String.format("[%s] %s", format.format(new Date()), what));
        // TODO Needs moar file IO.
    }
}