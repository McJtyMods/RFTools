package com.mcjty.rftools.items.netmonitor;

import net.minecraftforge.common.config.Configuration;

public class NetworkMonitorConfiguration {
    public static final String CATEGORY_NETWORK_MONITOR = "NetworkMonitor";
    public static int hilightTime = 5;

    public static void init(Configuration cfg) {
        hilightTime = cfg.get(CATEGORY_NETWORK_MONITOR, "hilightTime", hilightTime, "Time (in seconds) to hilight a block in the world").getInt();
    }
}
