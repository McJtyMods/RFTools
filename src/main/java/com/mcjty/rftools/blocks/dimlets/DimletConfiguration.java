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
    public static int EDITOR_MAXENERGY = 5000000;
    public static int EDITOR_RECEIVEPERTICK = 50000;
    public static int MAX_DIMENSION_POWER = 40000000;
    public static int DIMPOWER_WARN0 = 6000000;     // This is only used for darkness calculations.
    public static int DIMPOWER_WARN1 = 4000000;
    public static int DIMPOWER_WARN2 = 1000000;
    public static int DIMPOWER_WARN3 = 500000;
    public static int DIMPOWER_WARN_TP = 500000;    // Warn level for teleporter device.
    public static int INFUSER_MAXENERGY = 60000;
    public static int INFUSER_RECEIVEPERTICK = 200;
    public static int rfInfuseOperation = 600;
    public static int maxInfuse = 256;
    public static int dungeonChance = 200;
    public static int dimensionDifficulty = 1;      // -1 == whimpy, 0 == easy, 1 == normal
    public static int spawnDimension = 0;           // Dimension to return too when power runs out
    public static int cavernHeightLimit = 1;        // 0 == 64, 1 == 128, 2 == 195, 3 == 256
    public static float afterCreationCostFactor = 0.1f;
    public static float maintenanceCostPercentage = 0.0f;   // Bonus percentage in the dimlet cost.

    public static int PHASEDFIELD_MAXENERGY = 1000000;
    public static int PHASEDFIELD_RECEIVEPERTICK = 1000;
    public static int PHASEDFIELD_CONSUMEPERTICK = 100;

    public static float randomFeatureChance = 0.4f;
    public static float randomLakeFluidChance = 0.2f;
    public static float randomOrbFluidChance = 0.2f;
    public static float randomOregenMaterialChance = 0.2f;
    public static float randomFeatureMaterialChance = 0.4f;
    public static float randomStructureChance = 0.2f;
    public static float randomEffectChance = 0.1f;
    public static float randomOceanLiquidChance = 0.2f;
    public static float randomBaseBlockChance = 0.3f;
    public static float randomSpecialSkyChance = 0.5f;
    public static float randomExtraMobsChance = 0.4f;
    public static float randomSpecialTimeChance = 0.5f;
    public static float randomControllerChance = 0.4f;

    public static int bedrockLayer = 1;

	public static boolean randomizeSeed = false;



	public static void init(Configuration cfg) {
        PHASEDFIELD_MAXENERGY = cfg.get(CATEGORY_DIMLETS, "phasedFieldMaxRF", PHASEDFIELD_MAXENERGY,
                "Maximum RF storage that the phased field generator item can hold").getInt();
        PHASEDFIELD_RECEIVEPERTICK = cfg.get(CATEGORY_DIMLETS, "phasedFieldRFPerTick", PHASEDFIELD_RECEIVEPERTICK,
                "RF per tick that the the phased field generator item can receive").getInt();
        PHASEDFIELD_CONSUMEPERTICK = cfg.get(CATEGORY_DIMLETS, "phasedFieldConsumePerTick", PHASEDFIELD_CONSUMEPERTICK,
                "RF per tick that the the phased field generator item will consume").getInt();

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
        EDITOR_MAXENERGY = cfg.get(CATEGORY_DIMLETS, "dimensionEditorMaxRF", EDITOR_MAXENERGY,
                "Maximum RF storage that the dimension editor can hold").getInt();
        EDITOR_RECEIVEPERTICK = cfg.get(CATEGORY_DIMLETS, "dimensionEditorRFPerTick", EDITOR_RECEIVEPERTICK,
                "RF per tick that the the dimension editor can receive").getInt();
        MAX_DIMENSION_POWER = cfg.get(CATEGORY_DIMLETS, "dimensionPower", MAX_DIMENSION_POWER,
                "The internal RF buffer for every dimension").getInt();
        DIMPOWER_WARN0 = cfg.get(CATEGORY_DIMLETS, "dimensionPowerWarn0", DIMPOWER_WARN0,
                "The zero level at which power warning signs are starting to happen. This is only used for lighting level. No other debuffs occur at this level.").getInt();
        DIMPOWER_WARN1 = cfg.get(CATEGORY_DIMLETS, "dimensionPowerWarn1", DIMPOWER_WARN1,
                "The first level at which power warning signs are starting to happen").getInt();
        DIMPOWER_WARN2 = cfg.get(CATEGORY_DIMLETS, "dimensionPowerWarn2", DIMPOWER_WARN2,
                "The second level at which power warning signs are starting to become worse").getInt();
        DIMPOWER_WARN3 = cfg.get(CATEGORY_DIMLETS, "dimensionPowerWarn3", DIMPOWER_WARN3,
                "The third level at which power warning signs are starting to be very bad").getInt();
        DIMPOWER_WARN_TP = cfg.get(CATEGORY_DIMLETS, "dimensionPowerWarnTP", DIMPOWER_WARN_TP,
                "The level at which the teleportation system will consider a destination to be dangerous").getInt();

        INFUSER_MAXENERGY = cfg.get(CATEGORY_DIMLETS, "machineInfuserMaxRF", INFUSER_MAXENERGY,
                "Maximum RF storage that the machine infuser can hold").getInt();
        INFUSER_RECEIVEPERTICK = cfg.get(CATEGORY_DIMLETS, "machineInfuserRFPerTick", INFUSER_RECEIVEPERTICK,
                "RF per tick that the the machine infuser can receive").getInt();
        rfInfuseOperation = cfg.get(CATEGORY_DIMLETS, "dimletScramblerRFPerOperation", rfInfuseOperation,
                "RF that the machine infuser needs for one operation").getInt();
        afterCreationCostFactor = (float) cfg.get(CATEGORY_DIMLETS, "afterCreationCostFactor", afterCreationCostFactor,
                "If the dimension turns out to be more expensive after creation you get a factor of the actual cost extra to the RF/tick maintenance cost. If this is 0 there is no such cost. If this is 1 then you get the full cost").getDouble();
        maintenanceCostPercentage = (float) cfg.get(CATEGORY_DIMLETS, "maintenanceCostPercentage", maintenanceCostPercentage,
                "Percentage to add or subtract to the maintenance cost of all dimlets (100 would double the cost, -100 would set the cost to almost zero (complete zero is not allowed))").getDouble();

        maxInfuse = cfg.get(CATEGORY_DIMLETS, "maxInfuse", maxInfuse,
                "The maximum amount of dimensional shards that can be infused in a single machine").getInt();

        dungeonChance = cfg.get(CATEGORY_DIMLETS, "dungeonChance", dungeonChance,
                "The chance for a dungeon to spawn in a chunk. Higher numbers mean less chance (1 in 'dungeonChance' chance)").getInt();
        dimensionDifficulty = cfg.get(CATEGORY_DIMLETS, "difficulty", dimensionDifficulty,
                "Difficulty level for the dimension system. -1 means dimensions don't consume power. 0 means that you will not get killed but kicked out of the dimension when it runs out of power. 1 means certain death").getInt();
        spawnDimension = cfg.get(CATEGORY_DIMLETS, "spawnDimension", spawnDimension,
                "Dimension to respawn in after you get kicked out of an RFTools dimension").getInt();
        cavernHeightLimit = cfg.get(CATEGORY_DIMLETS, "cavernHeightLimit", cavernHeightLimit,
                "Maximum height of the caverns. 0=64, 1=128, 2=196, 3=256").getInt();

        randomFeatureChance = (float) cfg.get(CATEGORY_DIMLETS, "randomFeatureChance", randomFeatureChance,
                "The chance that every specific feature gets randomly selected in worldgen (tendrils, caves, lakes, oregen, ...)").getDouble();
        randomLakeFluidChance = (float) cfg.get(CATEGORY_DIMLETS, "randomLakeFluidChance", randomLakeFluidChance,
                "The chance that random fluid liquids are selected for lakes").getDouble();
        randomOrbFluidChance = (float) cfg.get(CATEGORY_DIMLETS, "randomOrbFluidChance", randomOrbFluidChance,
                "The chance that random fluid liquids are selected for liquid orbs").getDouble();
        randomOregenMaterialChance = (float) cfg.get(CATEGORY_DIMLETS, "randomOregenMaterialChance", randomOregenMaterialChance,
                "The chance that random blocks are selected for extra oregen feature").getDouble();
        randomFeatureMaterialChance = (float) cfg.get(CATEGORY_DIMLETS, "randomFeatureMaterialChance", randomFeatureMaterialChance,
                "The chance that random blocks are selected for landscape features (tendrils, canyons, ...)").getDouble();
        randomStructureChance = (float) cfg.get(CATEGORY_DIMLETS, "randomStructureChance", randomStructureChance,
                "The chance that every specific structure gets randomly selected in worldgen (village, nether fortress, ...)").getDouble();
        randomEffectChance = (float) cfg.get(CATEGORY_DIMLETS, "randomEffectChance", randomEffectChance,
                "The chance that an effect gets randomly selected in worldgen (poison, regeneration, ...)").getDouble();
        randomOceanLiquidChance = (float) cfg.get(CATEGORY_DIMLETS, "randomOceanLiquidChance", randomOceanLiquidChance,
                "The chance that a non-water block is selected for oceans and seas").getDouble();
        randomBaseBlockChance = (float) cfg.get(CATEGORY_DIMLETS, "randomBaseBlockChance", randomBaseBlockChance,
                "The chance that a non-stone block is selected for the main terrain").getDouble();
        randomSpecialSkyChance = (float) cfg.get(CATEGORY_DIMLETS, "randomSpecialSkyChance", randomSpecialSkyChance,
                "The chance that special sky features are selected").getDouble();
        randomExtraMobsChance = (float) cfg.get(CATEGORY_DIMLETS, "randomExtraMobsChance", randomExtraMobsChance,
                "The chance that extra specific mobs will spawn").getDouble();
        randomSpecialTimeChance = (float) cfg.get(CATEGORY_DIMLETS, "randomSpecialTimeChance", randomSpecialTimeChance,
                "The chance that special time features are selected").getDouble();
        randomControllerChance = (float) cfg.get(CATEGORY_DIMLETS, "randomControllerChance", randomControllerChance,
                "The chance that a random biome controller is selected").getDouble();

        bedrockLayer = cfg.get(CATEGORY_DIMLETS, "bedrockLayer", bedrockLayer,
                "The height of the bedrock layer that is generated at the bottom of some world types. Set to 0 to disable this and get default bedrock generation").getInt();

		randomizeSeed = cfg.get(CATEGORY_DIMLETS, "randomizeSeed", randomizeSeed,
			"Randomize the seed when the dimension is created").getBoolean();
    }

}
