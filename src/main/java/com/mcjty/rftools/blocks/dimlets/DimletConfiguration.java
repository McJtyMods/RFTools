package com.mcjty.rftools.blocks.dimlets;

import net.minecraftforge.common.config.Configuration;

public class DimletConfiguration {
    public static final String CATEGORY_DIMLETS = "dimlets";
    public static int RESEARCHER_MAXENERGY = 32000;
    public static int RESEARCHER_RECEIVEPERTICK = 80;
    public static int rfResearchOperation = 100;
    public static int SCRAMBLER_MAXENERGY = 32000;
    public static int SCRAMBLER_RECEIVEPERTICK = 80;
    public static int rfScrambleOperation = 100;
    public static int BUILDER_MAXENERGY = 10000000;
    public static int BUILDER_RECEIVEPERTICK = 50000;
    public static int MAX_DIMENSION_POWER = 10000000;
    public static int INFUSER_MAXENERGY = 60000;
    public static int INFUSER_RECEIVEPERTICK = 200;
    public static int rfInfuseOperation = 600;
    public static int maxInfuse = 256;
    public static int dungeonChance = 160;


    public static void init(Configuration cfg) {
        RESEARCHER_MAXENERGY = cfg.get(CATEGORY_DIMLETS, "dimletResearcherMaxRF", RESEARCHER_MAXENERGY,
                "Maximum RF storage that the dimlet researcher can hold").getInt();
        RESEARCHER_RECEIVEPERTICK = cfg.get(CATEGORY_DIMLETS, "dimletResearcherRFPerTick", RESEARCHER_RECEIVEPERTICK,
                "RF per tick that the the dimlet researcher can receive").getInt();
        rfResearchOperation = cfg.get(CATEGORY_DIMLETS, "dimletResearcherRFPerOperation", rfResearchOperation,
                "RF that the dimlet researcher needs for researching a single unknown dimlet").getInt();

        SCRAMBLER_MAXENERGY = cfg.get(CATEGORY_DIMLETS, "dimletScramblerMaxRF", SCRAMBLER_MAXENERGY,
                "Maximum RF storage that the dimlet scrambler can hold").getInt();
        SCRAMBLER_RECEIVEPERTICK = cfg.get(CATEGORY_DIMLETS, "dimletScramblerRFPerTick", SCRAMBLER_RECEIVEPERTICK,
                "RF per tick that the the dimlet scrambler can receive").getInt();
        rfScrambleOperation = cfg.get(CATEGORY_DIMLETS, "dimletScramblerRFPerOperation", rfScrambleOperation,
                "RF that the dimlet scrambler needs for one operation").getInt();

        BUILDER_MAXENERGY = cfg.get(CATEGORY_DIMLETS, "dimensionBuilderMaxRF", BUILDER_MAXENERGY,
                "Maximum RF storage that the dimension builder can hold").getInt();
        BUILDER_RECEIVEPERTICK = cfg.get(CATEGORY_DIMLETS, "dimensionBuilderRFPerTick", BUILDER_RECEIVEPERTICK,
                "RF per tick that the the dimension builder can receive").getInt();
        MAX_DIMENSION_POWER = cfg.get(CATEGORY_DIMLETS, "dimensionPower", MAX_DIMENSION_POWER,
                "The internal RF buffer for every dimension").getInt();

        INFUSER_MAXENERGY = cfg.get(CATEGORY_DIMLETS, "machineInfuserMaxRF", INFUSER_MAXENERGY,
                "Maximum RF storage that the machine infuser can hold").getInt();
        INFUSER_RECEIVEPERTICK = cfg.get(CATEGORY_DIMLETS, "machineInfuserRFPerTick", INFUSER_RECEIVEPERTICK,
                "RF per tick that the the machine infuser can receive").getInt();
        rfInfuseOperation = cfg.get(CATEGORY_DIMLETS, "dimletScramblerRFPerOperation", rfInfuseOperation,
                "RF that the machine infuser needs for one operation").getInt();

        maxInfuse = cfg.get(CATEGORY_DIMLETS, "maxInfuse", maxInfuse,
                "The maximum amount of dimensional shards that can be infused in a single machine").getInt();

        dungeonChance = cfg.get(CATEGORY_DIMLETS, "dungeonChance", dungeonChance,
                "The chance for a dungeon to spawn in a chunk. Higher numbers mean less chance (1 in 'dungeonChance' chance)").getInt();
    }

}
