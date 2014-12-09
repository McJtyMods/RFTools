package com.mcjty.rftools;

import net.minecraftforge.common.config.Configuration;

public class GeneralConfiguration {
    public static final String CATEGORY_GENERAL = "general";

    public static boolean doLogging = false;

    public static void init(Configuration cfg) {
        doLogging = cfg.get(CATEGORY_GENERAL, "logging", doLogging,
                "If true dump a lot of logging information about various things in RFTools. Useful for debugging.").getBoolean();
    }

}
