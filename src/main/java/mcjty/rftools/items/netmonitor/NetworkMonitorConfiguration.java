package mcjty.rftools.items.netmonitor;

import mcjty.lib.thirteen.ConfigSpec;

public class NetworkMonitorConfiguration {
    public static final String CATEGORY_NETWORK_MONITOR = "networkmonitor";
    public static ConfigSpec.BooleanValue enabled;
    public static ConfigSpec.IntValue hilightTime;
    public static ConfigSpec.IntValue maximumBlocks;

    public static void init(ConfigSpec.Builder SERVER_BUILDER, ConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Settings for the network monitor item").push(CATEGORY_NETWORK_MONITOR);
        CLIENT_BUILDER.comment("Settings for the network monitor item").push(CATEGORY_NETWORK_MONITOR);

        enabled = SERVER_BUILDER
                .comment("Whether the network monitor should exist")
                .define("enabled", true);
        hilightTime = SERVER_BUILDER
                .comment("Time (in seconds) to hilight a block in the world")
                .defineInRange("hilightTime", 5, 0, Integer.MAX_VALUE);
        maximumBlocks = SERVER_BUILDER
                .comment("Maximum amount of blocks to show in monitor (do NOT increase above 1800!)")
                .defineInRange("maximumBlocks", 500, 0, 1800);

        SERVER_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }
}
