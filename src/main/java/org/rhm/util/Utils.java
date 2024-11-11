package org.rhm.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.text.DecimalFormat;

public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    private static final boolean useProfiler = logger.isEnabledForLevel(Level.DEBUG);

    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";

    public static void profile(Runnable part, String name) {
        if (!useProfiler) {
            part.run();
        } else {
            logger.info(ANSI_GREEN + "------ Starting profiling for {} ------" + ANSI_RESET, name);

            long startTime = System.nanoTime();
            part.run();
            long endTime = System.nanoTime();

            logger.info(ANSI_GREEN + "------ Ending profiling for {} ------" + ANSI_RESET, name);

            long durationNs = endTime - startTime;
            double durationSecs = durationNs / 1_000_000_000.0;

            DecimalFormat df = new DecimalFormat("#.####");
            String formattedDurationSecs = df.format(durationSecs);

            logger.info(ANSI_GREEN + "Execution time for {}: {} ns ({} seconds)" + ANSI_RESET, name, durationNs, formattedDurationSecs);
        }
    }
}
