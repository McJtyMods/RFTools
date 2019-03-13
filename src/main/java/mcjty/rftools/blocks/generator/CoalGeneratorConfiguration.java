package mcjty.rftools.blocks.generator;

import mcjty.lib.thirteen.ConfigSpec;

public class CoalGeneratorConfiguration {

    public static final String CATEGORY_COALGEN = "coalgen";

    public static ConfigSpec.BooleanValue enabled;
    public static ConfigSpec.IntValue MAXENERGY; // TODO change these to longs once Configuration supports them
    public static ConfigSpec.IntValue SENDPERTICK;
    public static ConfigSpec.IntValue CHARGEITEMPERTICK;
    public static ConfigSpec.IntValue rfPerTick;
    public static ConfigSpec.IntValue ticksPerCoal;

    public static void init(ConfigSpec.Builder SERVER_BUILDER, ConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Settings for the coal generator").push(CATEGORY_COALGEN);
        CLIENT_BUILDER.comment("Settings for the coal generator").push(CATEGORY_COALGEN);

        enabled = SERVER_BUILDER
                .comment("Whether the coal generator should exist")
                .define("enabled", true);

        rfPerTick = SERVER_BUILDER
                .comment("Amount of RF generated per tick")
                .defineInRange("generatePerTick", 60, 0, Integer.MAX_VALUE);
        ticksPerCoal = SERVER_BUILDER
                .comment("Amount of ticks generated per coal")
                .defineInRange("ticksPerCoal", 600, 0, Integer.MAX_VALUE);
        MAXENERGY = SERVER_BUILDER
                .comment("Maximum RF storage that the generator can hold")
                .defineInRange("generatorMaxRF", 500000, 0, Integer.MAX_VALUE);
        SENDPERTICK = SERVER_BUILDER
                .comment("RF per tick that the generator can send")
                .defineInRange("generatorRFPerTick", 2000, 0, Integer.MAX_VALUE);
        CHARGEITEMPERTICK = SERVER_BUILDER
                .comment("RF per tick that the generator can charge items with")
                .defineInRange("generatorChargePerTick", 1000, 0, Integer.MAX_VALUE);

        SERVER_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }
}
