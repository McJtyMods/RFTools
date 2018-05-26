package mcjty.rftools.blocks.generator;

import net.minecraftforge.common.config.Configuration;

public class CoalGeneratorConfiguration {

    public static final String CATEGORY_COALGEN = "coalgen";
    public static boolean enabled = true;
    public static long MAXENERGY = 500000;
    public static long SENDPERTICK = 2000;
    public static long CHARGEITEMPERTICK = 1000;
    public static long rfPerTick = 60;
    public static int ticksPerCoal = 600;

    public static void init(Configuration cfg) {
        enabled = cfg.get(CATEGORY_COALGEN, "enabled", enabled, "Whether the coal generator should exist").getBoolean();
        rfPerTick = cfg.get(CATEGORY_COALGEN, "generatePerTick", rfPerTick, "Amount of RF generated per tick").getLong();
        ticksPerCoal = cfg.get(CATEGORY_COALGEN, "ticksPerCoal", ticksPerCoal, "Amount of ticks generated per coal").getInt();
        MAXENERGY = cfg.get(CATEGORY_COALGEN, "generatorMaxRF", MAXENERGY,
                "Maximum RF storage that the generator can hold").getLong();
        SENDPERTICK = cfg.get(CATEGORY_COALGEN, "generatorRFPerTick", SENDPERTICK,
                              "RF per tick that the generator can send").getLong();
        CHARGEITEMPERTICK = cfg.get(CATEGORY_COALGEN, "generatorChargePerTick", CHARGEITEMPERTICK,
                              "RF per tick that the generator can charge items with").getLong();
    }
}
