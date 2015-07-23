package mcjty.varia;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Logging {
    private static Logging instance = null;

    public static long prevTicks = -1;
    private Logger logger;

    public static boolean debugMode = false;
    public static boolean doLogging = false;

    private Logging() {
        logger = LogManager.getLogger();
        instance = this;
    }

    public static void logError(String msg) {
        if (instance == null) {
            instance = new Logging();
        }
        instance.logger.log(Level.ERROR, msg);
    }

    public static void log(World world, TileEntity te, String message) {
        if (doLogging) {
            long ticks = world.getTotalWorldTime();
            if (ticks != prevTicks) {
                prevTicks = ticks;
                instance.logger.log(Level.INFO, "=== Time " + ticks + " ===");
            }
            String id = te.xCoord + "," + te.yCoord + "," + te.zCoord + ": ";
            instance.logger.log(Level.INFO, id + message);
        }
    }

    public static void log(String message) {
        instance.logger.log(Level.INFO, message);
    }

    public static void logDebug(String message) {
        if (debugMode) {
            instance.logger.log(Level.INFO, message);
        }
    }
}
