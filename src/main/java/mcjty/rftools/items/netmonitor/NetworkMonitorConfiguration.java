package mcjty.rftools.items.netmonitor;


import net.minecraftforge.common.ForgeConfigSpec;

public class NetworkMonitorConfiguration {
    public static final String CATEGORY_NETWORK_MONITOR = "networkmonitor";
    public static ForgeConfigSpec.BooleanValue enabled;
    public static ForgeConfigSpec.IntValue hilightTime;
    public static ForgeConfigSpec.IntValue maximumBlocks;

    public static void init(ForgeConfigSpec.Builder SERVER_BUILDER, ForgeConfigSpec.Builder CLIENT_BUILDER) {
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
