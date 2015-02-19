package com.mcjty.rftools.blocks.environmental;

import net.minecraftforge.common.config.Configuration;

public class EnvironmentalConfiguration {
    public static final String CATEGORY_ENVIRONMENTAL = "environmental";
    public static int ENVIRONMENTAL_MAXENERGY = 500000;
    public static int ENVIRONMENTAL_RECEIVEPERTICK = 10000;

    public static void init(Configuration cfg) {
        ENVIRONMENTAL_MAXENERGY = cfg.get(CATEGORY_ENVIRONMENTAL, "environmentalMaxRF", ENVIRONMENTAL_MAXENERGY,
                "Maximum RF storage that the environmental controller can hold").getInt();
        ENVIRONMENTAL_RECEIVEPERTICK = cfg.get(CATEGORY_ENVIRONMENTAL, "environmentalRFPerTick", ENVIRONMENTAL_RECEIVEPERTICK,
                "RF per tick that the the environmental controller can receive").getInt();
    }
}
