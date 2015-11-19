package mcjty.rftools.blocks.dimlets;

import net.minecraftforge.common.config.Configuration;

public class DimletConfiguration {
    public static final String CATEGORY_DIMLETS = "dimlets";
    public static int EXTRACTOR_MAXENERGY = 50000;
    public static int EXTRACTOR_SENDPERTICK = 1000;
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
    public static int dungeonChance = 200;
    public static int volcanoChance = 60;
    public static int dimensionDifficulty = 1;      // -1 == whimpy, 0 == easy, 1 == normal
    public static int spawnDimension = 0;           // Dimension to return too when power runs out
    public static boolean respawnSameDim = false;   // If true we first try to respawn in rftools dimension unless power is low.
    public static boolean freezeUnpowered = true;   // Freeze all entities and TE's in an unpowered dimension.
    public static boolean preventSpawnUnpowered = true; // Prevent spawns in unpowered dimensions
    public static double brutalMobsFactor = 5.0f;   // How much stronger brutal mobs should be
    public static double strongMobsFactor = 2.0f;   // How much stronger brutal strong should be

    public static int cavernHeightLimit = 1;        // 0 == 64, 1 == 128, 2 == 195, 3 == 256
    public static float afterCreationCostFactor = 0.1f;
    public static float maintenanceCostPercentage = 0.0f;   // Bonus percentage in the dimlet cost.
    public static int minimumCostPercentage = 10;   // Bonus dimlets (efficiency and related) can at most reduce cost to 10% by default

    public static int PHASEDFIELD_MAXENERGY = 1000000;
    public static int PHASEDFIELD_RECEIVEPERTICK = 1000;
    public static int PHASEDFIELD_CONSUMEPERTICK = 100;
    public static int phasedFieldGeneratorRange = 5;
    public static boolean phasedFieldGeneratorDebuf = true;

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
    public static float randomWeatherChance = 0.8f;
    public static float randomControllerChance = 0.4f;

    public static int bedrockLayer = 1;
    public static int bedBehaviour = 0;         // Behaviour when sleeping in an RFTools dimension: 0 = do nothing, 1 = explode, 2 = set spawn

	public static boolean randomizeSeed = false;
    public static boolean normalTerrainInheritsOverworld = false;

    // Server owner configs
    public static boolean voidOnly = false;
    public static boolean ownerDimletsNeeded = false;
    public static boolean dimensionBuilderNeedsOwner = false;
    public static boolean playersCanDeleteDimensions = false;
    public static boolean dimensionFolderIsDeletedWithSafeDel = true;

    public static float endermanUnknownDimletDrop = 0.01f;
    public static int unknownDimletChestLootMinimum = 1;
    public static int unknownDimletChestLootMaximum = 3;
    public static int unknownDimletChestLootRarity = 50;

    public static int dimletStackSize = 16;

    public static boolean dimensionalShardRecipe = false;

	public static void init(Configuration cfg) {
        EXTRACTOR_MAXENERGY = cfg.get(CATEGORY_DIMLETS, "energyExtractorMaxRF", EXTRACTOR_MAXENERGY,
                "Maximum RF storage that the energy extractor can hold").getInt();
        EXTRACTOR_SENDPERTICK = cfg.get(CATEGORY_DIMLETS, "energyExtractorRFPerTick", EXTRACTOR_SENDPERTICK,
                "RF per tick that the energy extractor can send").getInt();

        PHASEDFIELD_MAXENERGY = cfg.get(CATEGORY_DIMLETS, "phasedFieldMaxRF", PHASEDFIELD_MAXENERGY,
                "Maximum RF storage that the phased field generator item can hold").getInt();
        PHASEDFIELD_RECEIVEPERTICK = cfg.get(CATEGORY_DIMLETS, "phasedFieldRFPerTick", PHASEDFIELD_RECEIVEPERTICK,
                "RF per tick that the phased field generator item can receive").getInt();
        PHASEDFIELD_CONSUMEPERTICK = cfg.get(CATEGORY_DIMLETS, "phasedFieldConsumePerTick", PHASEDFIELD_CONSUMEPERTICK,
                "RF per tick that the phased field generator item will consume").getInt();
        phasedFieldGeneratorRange = cfg.get(CATEGORY_DIMLETS, "phasedFieldGeneratorRange", phasedFieldGeneratorRange,
                "In this range the PFG will keep entities active (set to 0 to disable this feature)").getInt();
        phasedFieldGeneratorDebuf = cfg.get(CATEGORY_DIMLETS, "phasedFieldGeneratorDebuf", phasedFieldGeneratorDebuf,
                "If true you will get some debufs when the PFG is in use. If false there will be no debufs").getBoolean();

        RESEARCHER_MAXENERGY = cfg.get(CATEGORY_DIMLETS, "dimletResearcherMaxRF", RESEARCHER_MAXENERGY,
                "Maximum RF storage that the dimlet researcher can hold").getInt();
        RESEARCHER_RECEIVEPERTICK = cfg.get(CATEGORY_DIMLETS, "dimletResearcherRFPerTick", RESEARCHER_RECEIVEPERTICK,
                "RF per tick that the dimlet researcher can receive").getInt();
        rfResearchOperation = cfg.get(CATEGORY_DIMLETS, "dimletResearcherRFPerOperation", rfResearchOperation,
                "RF that the dimlet researcher needs for researching a single unknown dimlet").getInt();

        SCRAMBLER_MAXENERGY = cfg.get(CATEGORY_DIMLETS, "dimletScramblerMaxRF", SCRAMBLER_MAXENERGY,
                "Maximum RF storage that the dimlet scrambler can hold").getInt();
        SCRAMBLER_RECEIVEPERTICK = cfg.get(CATEGORY_DIMLETS, "dimletScramblerRFPerTick", SCRAMBLER_RECEIVEPERTICK,
                "RF per tick that the dimlet scrambler can receive").getInt();
        rfScrambleOperation = cfg.get(CATEGORY_DIMLETS, "dimletScramblerRFPerOperation", rfScrambleOperation,
                "RF that the dimlet scrambler needs for one operation").getInt();

        BUILDER_MAXENERGY = cfg.get(CATEGORY_DIMLETS, "dimensionBuilderMaxRF", BUILDER_MAXENERGY,
                "Maximum RF storage that the dimension builder can hold").getInt();
        BUILDER_RECEIVEPERTICK = cfg.get(CATEGORY_DIMLETS, "dimensionBuilderRFPerTick", BUILDER_RECEIVEPERTICK,
                "RF per tick that the dimension builder can receive").getInt();
        EDITOR_MAXENERGY = cfg.get(CATEGORY_DIMLETS, "dimensionEditorMaxRF", EDITOR_MAXENERGY,
                "Maximum RF storage that the dimension editor can hold").getInt();
        EDITOR_RECEIVEPERTICK = cfg.get(CATEGORY_DIMLETS, "dimensionEditorRFPerTick", EDITOR_RECEIVEPERTICK,
                "RF per tick that the dimension editor can receive").getInt();
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
        minimumCostPercentage = cfg.get(CATEGORY_DIMLETS, "minimumCostPercentage", minimumCostPercentage,
                "Bonus dimlets can never get the maintenance cost of a dimension below this percentage of the nominal cost without bonus dimlets").getInt();

        dungeonChance = cfg.get(CATEGORY_DIMLETS, "dungeonChance", dungeonChance,
                "The chance for a dungeon to spawn in a chunk. Higher numbers mean less chance (1 in 'dungeonChance' chance)").getInt();
        volcanoChance = cfg.get(CATEGORY_DIMLETS, "volcanoChance", volcanoChance,
                "The chance for a volcano to spawn in a chunk (with the volcano feature dimlet). Higher numbers mean less chance (1 in 'volcanoChance' chance)").getInt();
        dimensionDifficulty = cfg.get(CATEGORY_DIMLETS, "difficulty", dimensionDifficulty,
                "Difficulty level for the dimension system. -1 means dimensions don't consume power. 0 means that you will not get killed but kicked out of the dimension when it runs out of power. 1 means certain death").getInt();
        spawnDimension = cfg.get(CATEGORY_DIMLETS, "spawnDimension", spawnDimension,
                "Dimension to respawn in after you get kicked out of an RFTools dimension").getInt();
        respawnSameDim = cfg.get(CATEGORY_DIMLETS, "respawnRfToolsDimension", respawnSameDim,
                "If this flag is true the player will respawn in the rftools dimension when he dies (unless power runs out)").getBoolean();
        freezeUnpowered = cfg.get(CATEGORY_DIMLETS, "freezeUnpoweredDimension", freezeUnpowered,
                "If this flag is true RFTools will freeze all entities and machines in a dimension when the power runs out").getBoolean();
        preventSpawnUnpowered = cfg.get(CATEGORY_DIMLETS, "preventSpawnUnpoweredDimension", preventSpawnUnpowered,
                "If this flag is true all spawns will be disabled in an unpowered dimension").getBoolean();
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
                "The chance that default time features are selected").getDouble();
        randomWeatherChance = (float) cfg.get(CATEGORY_DIMLETS, "randomWeatherChance", randomWeatherChance,
                "The chance that default weather features are selected").getDouble();
        randomControllerChance = (float) cfg.get(CATEGORY_DIMLETS, "randomControllerChance", randomControllerChance,
                "The chance that a random biome controller is selected").getDouble();

        endermanUnknownDimletDrop = (float) cfg.get(CATEGORY_DIMLETS, "endermanUnknownDimletDrop", endermanUnknownDimletDrop,
                "The chance that you get an unknown dimlet when killing an enderman. Set to 0 to disable").getDouble();
        unknownDimletChestLootMinimum = cfg.get(CATEGORY_DIMLETS, "unknownDimletChestLootMinimum", unknownDimletChestLootMinimum,
                "The minimum amount of unknown dimlets that can be generated in a dungeon chest").getInt();
        unknownDimletChestLootMaximum = cfg.get(CATEGORY_DIMLETS, "unknownDimletChestLootMaximum", unknownDimletChestLootMaximum,
                "The maximum amount of unknown dimlets that can be generated in a dungeon chest").getInt();
        unknownDimletChestLootRarity = cfg.get(CATEGORY_DIMLETS, "unknownDimletChestLootRarity", unknownDimletChestLootRarity,
                "The rarity of unknown dimlets in dungeon chests (0 means you'll get none, 100 means very common)").getInt();

        bedrockLayer = cfg.get(CATEGORY_DIMLETS, "bedrockLayer", bedrockLayer,
                "The height of the bedrock layer that is generated at the bottom of some world types. Set to 0 to disable this and get default bedrock generation").getInt();
        bedBehaviour = cfg.get(CATEGORY_DIMLETS, "bedBehaviour", bedBehaviour,
                "Behaviour when sleeping in an RFTools dimension: 0 = do nothing, 1 = explode, 2 = set spawn").getInt();

        randomizeSeed = cfg.get(CATEGORY_DIMLETS, "randomizeSeed", randomizeSeed,
                "Randomize the seed when the dimension is created").getBoolean();
        normalTerrainInheritsOverworld = cfg.get(CATEGORY_DIMLETS, "normalTerrainInheritsOverworld", normalTerrainInheritsOverworld,
                "Set this to true if you want terrains with dimlet 'normal' to generate like the overworld (i.e. amplified if the overworld is amplified)").getBoolean();

        dimensionalShardRecipe = cfg.get(CATEGORY_DIMLETS, "dimensionalShardRecipe", dimensionalShardRecipe,
                "Set this to true if you want a recipe for dimensional shards. Useful on servers that disallow dimensions").getBoolean();
        voidOnly = cfg.get(CATEGORY_DIMLETS, "voidOnly", voidOnly,
                "Set this to true if you want to make sure RFTools can only create void dimensions").getBoolean();
        ownerDimletsNeeded = cfg.get(CATEGORY_DIMLETS, "ownerDimletsNeeded", ownerDimletsNeeded,
                "If this is enabled (non-craftable) owner dimlets are required to construct dimension tabs. This is useful on servers where you want to limit the amount of dimensions a player can make").getBoolean();
        dimensionBuilderNeedsOwner = cfg.get(CATEGORY_DIMLETS, "dimensionBuilderNeedsOwner", dimensionBuilderNeedsOwner,
                "If this is enabled then the dimension builder needs a correct owner before you can create dimensions with it").getBoolean();
        playersCanDeleteDimensions = cfg.get(CATEGORY_DIMLETS, "playersCanDeleteDimensions", playersCanDeleteDimensions,
                "If this is enabled then regular players can delete their own dimensions using the /rftdim safedel <id> command").getBoolean();
        dimensionFolderIsDeletedWithSafeDel = cfg.get(CATEGORY_DIMLETS, "dimensionFolderIsDeletedWithSafeDel", dimensionFolderIsDeletedWithSafeDel,
                "If this is enabled the /rftdim safedel <id> command will also delete the DIM<id> folder. If false then this has to be done manually").getBoolean();

        brutalMobsFactor = cfg.get(CATEGORY_DIMLETS, "brutalMobsFactor", brutalMobsFactor,
                "How much stronger mobs should be if spawned in a dimension with the brutal mobs dimlet").getDouble();
        strongMobsFactor = cfg.get(CATEGORY_DIMLETS, "strongMobsFactor", strongMobsFactor,
                "How much stronger mobs should be if spawned in a dimension with the strong mobs dimlet").getDouble();

        dimletStackSize = cfg.get(CATEGORY_DIMLETS, "dimletStackSize", dimletStackSize,
                "Stack limit for dimlets").getInt();
    }

}
