package com.mcjty.rftools.blocks.screens;

import net.minecraftforge.common.config.Configuration;

public class ScreenConfiguration {
    public static final String CATEGORY_SCREEN = "screen";
    public static int CONTROLLER_MAXENERGY = 60000;
    public static int CONTROLLER_RECEIVEPERTICK = 1000;


    public static void init(Configuration cfg) {
        CONTROLLER_MAXENERGY = cfg.get(CATEGORY_SCREEN, "screenControllerMaxRF", CONTROLLER_MAXENERGY,
                "Maximum RF storage that the screen controller can hold").getInt();
        CONTROLLER_RECEIVEPERTICK = cfg.get(CATEGORY_SCREEN, "dimletResearcherRFPerTick", CONTROLLER_RECEIVEPERTICK,
                "RF per tick that the the screen controller can receive").getInt();
    }

}
