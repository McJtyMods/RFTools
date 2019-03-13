package mcjty.rftools.blocks.crafter;

import mcjty.lib.thirteen.ConfigSpec;

public class CrafterConfiguration {

    public static final String CATEGORY_CRAFTER = "crafter";

    public static ConfigSpec.BooleanValue enabled;
    public static ConfigSpec.IntValue MAXENERGY;
    public static ConfigSpec.IntValue RECEIVEPERTICK;
    public static ConfigSpec.IntValue rfPerOperation;
    public static ConfigSpec.IntValue speedOperations;

    public static void init(ConfigSpec.Builder SERVER_BUILDER, ConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Settings for the crafter").push(CATEGORY_CRAFTER);
        CLIENT_BUILDER.comment("Settings for the crafter").push(CATEGORY_CRAFTER);

        enabled = SERVER_BUILDER
                .comment("Whether the crafter should exist")
                .define("enabled", true);

        rfPerOperation = SERVER_BUILDER
                .comment("Amount of RF used per crafting operation")
                .defineInRange("rfPerOperation", 100, 0, Integer.MAX_VALUE);
        speedOperations = SERVER_BUILDER
                .comment("How many operations to do at once in fast mode")
                .defineInRange("speedOperations", 5, 0, Integer.MAX_VALUE);
        MAXENERGY = SERVER_BUILDER
                .comment("Maximum RF storage that the crafter can hold")
                .defineInRange("crafterMaxRF", 50000, 0, Integer.MAX_VALUE);
        RECEIVEPERTICK = SERVER_BUILDER
                .comment("RF per tick that the crafter can receive")
                .defineInRange("crafterRFPerTick", 500, 0, Integer.MAX_VALUE);

        SERVER_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }
}
