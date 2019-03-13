package mcjty.rftools.items.netmonitor;

import net.minecraftforge.common.config.Configuration;

public class NetworkMonitorConfiguration {
    public static final String CATEGORY_NETWORK_MONITOR = "networkmonitor";
    public static boolean enabled = true;
    public static int hilightTime = 5;
    public static int maximumBlocks = 500;

    public static void init(Configuration cfg) {
        cfg.addCustomCategoryComment(NetworkMonitorConfiguration.CATEGORY_NETWORK_MONITOR, "Settings for the network monitor item");
        enabled = cfg.get(CATEGORY_NETWORK_MONITOR, "enabled", enabled, "Whether the network monitor should exist").getBoolean();
        hilightTime = cfg.get(CATEGORY_NETWORK_MONITOR, "hilightTime", hilightTime, "Time (in seconds) to hilight a block in the world").getInt();
        maximumBlocks = cfg.get(CATEGORY_NETWORK_MONITOR, "maximumBlocks", maximumBlocks, "Maximum amount of blocks to show in monitor (do NOT increase above 1800!)").getInt();
    }
}
