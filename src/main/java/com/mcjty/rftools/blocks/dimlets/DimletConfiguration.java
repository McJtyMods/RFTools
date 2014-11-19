package com.mcjty.rftools.blocks.dimlets;

import net.minecraftforge.common.config.Configuration;

public class DimletConfiguration {
    public static final String CATEGORY_DIMLETS = "Dimlets";
    public static int RESEARCHER_MAXENERGY = 32000;
    public static int RESEARCHER_RECEIVEPERTICK = 80;
    public static int rfResearchOperation = 100;
    public static int ENSCRIBER_MAXENERGY = 32000;
    public static int ENSCRIBER_RECEIVEPERTICK = 80;
    public static int BUILDER_MAXENERGY = 10000000;
    public static int BUILDER_RECEIVEPERTICK = 20000;

    public static void init(Configuration cfg) {
        RESEARCHER_MAXENERGY = cfg.get(CATEGORY_DIMLETS, "dimletResearcherMaxRF", RESEARCHER_MAXENERGY,
                "Maximum RF storage that the dimlet researcher can hold").getInt();
        RESEARCHER_RECEIVEPERTICK = cfg.get(CATEGORY_DIMLETS, "dimletResearcherRFPerTick", RESEARCHER_RECEIVEPERTICK,
                "RF per tick that the the dimlet researcher can receive").getInt();
        rfResearchOperation = cfg.get(CATEGORY_DIMLETS, "dimletResearcherRFPerOperation", rfResearchOperation,
                "RF that the dimlet researcher needs for researching a single unknown dimlet").getInt();
        ENSCRIBER_MAXENERGY = cfg.get(CATEGORY_DIMLETS, "dimensionEnscriberMaxRF", ENSCRIBER_MAXENERGY,
                "Maximum RF storage that the dimension enscriber can hold").getInt();
        ENSCRIBER_RECEIVEPERTICK = cfg.get(CATEGORY_DIMLETS, "dimensionEnscriberRFPerTick", ENSCRIBER_RECEIVEPERTICK,
                "RF per tick that the the dimension enscriber can receive").getInt();
        BUILDER_MAXENERGY = cfg.get(CATEGORY_DIMLETS, "dimensionBuilderMaxRF", BUILDER_MAXENERGY,
                "Maximum RF storage that the dimension builder can hold").getInt();
        BUILDER_RECEIVEPERTICK = cfg.get(CATEGORY_DIMLETS, "dimensionBuilderRFPerTick", BUILDER_RECEIVEPERTICK,
                "RF per tick that the the dimension builder can receive").getInt();
    }

}
