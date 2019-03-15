package mcjty.rftools.blocks.booster;

import mcjty.lib.thirteen.ConfigSpec;

public class BoosterConfiguration {
    public static final String CATEGORY_BOOSTER = "booster";

    public static ConfigSpec.IntValue BOOSTER_MAXENERGY;
    public static ConfigSpec.IntValue BOOSTER_RECEIVEPERTICK;
    public static ConfigSpec.DoubleValue energyMultiplier;

    public static void init(ConfigSpec.Builder SERVER_BUILDER, ConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Settings for the booster").push(CATEGORY_BOOSTER);
        CLIENT_BUILDER.comment("Settings for the booster").push(CATEGORY_BOOSTER);

        BOOSTER_MAXENERGY = SERVER_BUILDER
                .comment("Maximum RF storage that the booster can hold")
                .defineInRange("boosterMaxRF", 200000, 0, Integer.MAX_VALUE);
        BOOSTER_RECEIVEPERTICK = SERVER_BUILDER
                .comment("RF per tick that the the booster can receive")
                .defineInRange("boosterRFPerTick", 1000, 0, Integer.MAX_VALUE);
        energyMultiplier = SERVER_BUILDER
                .comment("Multiplier for the module to calculate RF consumption for a single usage")
                .defineInRange("energyMultiplier", 500000.0, 0.0, 1000000000.0);

        SERVER_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }
}
