package mcjty.rftools.blocks.booster;

import net.minecraftforge.common.config.Configuration;

public class BoosterConfiguration {
    public static final String CATEGORY_BOOSTER = "booster";
    public static int BOOSTER_MAXENERGY = 200000;
    public static int BOOSTER_RECEIVEPERTICK = 1000;
    public static float energyMultiplier = 500000;

    public static void init(Configuration cfg) {
        cfg.addCustomCategoryComment(BoosterConfiguration.CATEGORY_BOOSTER, "Settings for the booster");
        BOOSTER_MAXENERGY = cfg.get(CATEGORY_BOOSTER, "boosterMaxRF", BOOSTER_MAXENERGY,
                "Maximum RF storage that the booster can hold").getInt();
        BOOSTER_RECEIVEPERTICK = cfg.get(CATEGORY_BOOSTER, "boosterRFPerTick", BOOSTER_RECEIVEPERTICK,
                                         "RF per tick that the the booster can receive").getInt();
        energyMultiplier = (float) cfg.get(CATEGORY_BOOSTER, "energyMultiplier", energyMultiplier,
                                         "Multiplier for the module to calculate RF consumption for a single usage").getDouble();
    }
}
