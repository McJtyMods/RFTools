package com.mcjty.rftools.blocks.dimletconstruction;

import net.minecraftforge.common.config.Configuration;

public class DimletConstructionConfiguration {
    public static final String CATEGORY_DIMLET_CONSTRUCTION = "dimletconstruction";
    public static int WORKBENCH_MAXENERGY = 32000;
    public static int WORKBENCH_RECEIVEPERTICK = 80;
    public static int rfExtractOperation = 200;
    public static int maxBiomeAbsorbtion = 1000;    // Amount of ticks before a biome absorber is ready

    public static void init(Configuration cfg) {
        WORKBENCH_MAXENERGY = cfg.get(CATEGORY_DIMLET_CONSTRUCTION, "dimletWorkbenchMaxRF", WORKBENCH_MAXENERGY,
                "Maximum RF storage that the dimlet workbench can hold").getInt();
        WORKBENCH_RECEIVEPERTICK = cfg.get(CATEGORY_DIMLET_CONSTRUCTION, "dimletWorkbenchRFPerTick", WORKBENCH_RECEIVEPERTICK,
                "RF per tick that the the dimlet workbench can receive").getInt();
        rfExtractOperation = cfg.get(CATEGORY_DIMLET_CONSTRUCTION, "dimletWorkbenchRFPerOperation", rfExtractOperation,
                "RF that the dimlet workbench needs for extracting one dimlet").getInt();

        maxBiomeAbsorbtion = cfg.get(CATEGORY_DIMLET_CONSTRUCTION, "maxBiomeAbsorbtion", maxBiomeAbsorbtion,
                "Amount of ticks needed to fully absorbe a biome essence").getInt();


    }
}
