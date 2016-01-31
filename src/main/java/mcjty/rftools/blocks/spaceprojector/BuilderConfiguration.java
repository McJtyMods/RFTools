package mcjty.rftools.blocks.spaceprojector;

import net.minecraftforge.common.config.Configuration;

public class BuilderConfiguration {
    public static final String CATEGORY_BUILDER = "builder";

    public static int SPACEPROJECTOR_MAXENERGY = 100000;
    public static int SPACEPROJECTOR_RECEIVEPERTICK = 1000;

    public static int BUILDER_MAXENERGY = 10000000;
    public static int BUILDER_RECEIVEPERTICK = 50000;

    public static int builderRfPerOperation = 500;
    public static int builderRfPerQuarry = 300;
    public static int builderRfPerEntity = 5000;
    public static int builderRfPerPlayer = 40000;
    public static double dimensionCostFactor = 5.0f;

    public static BuilderTileEntityMode teMode = BuilderTileEntityMode.MOVE_WHITELIST;

    public static int maxSpaceChamberDimension = 128;

    public static double voidShapeCardFactor = 0.5;
    public static double silkquarryShapeCardFactor = 3;
    public static double fortunequarryShapeCardFactor = 2;

    public static boolean quarryChunkloads = true;
    public static boolean shapeCardAllowed = true;
    public static boolean quarryAllowed = true;
    public static boolean clearingQuarryAllowed = true;

    public static int quarryBaseSpeed = 8;
    public static int quarryInfusionSpeedFactor = 20;

    public static int maxBuilderOffset = 260;
    public static int maxBuilderDimension = 512;

    public static boolean oldSphereCylinderShape = false;

    public static void init(Configuration cfg) {
        SPACEPROJECTOR_MAXENERGY = cfg.get(CATEGORY_BUILDER, "spaceProjectorMaxRF", SPACEPROJECTOR_MAXENERGY,
                "Maximum RF storage that the space projector can hold").getInt();
        SPACEPROJECTOR_RECEIVEPERTICK = cfg.get(CATEGORY_BUILDER, "spaceProjectorRFPerTick", SPACEPROJECTOR_RECEIVEPERTICK,
                "RF per tick that the space projector can receive").getInt();
        BUILDER_MAXENERGY = cfg.get(CATEGORY_BUILDER, "builderMaxRF", BUILDER_MAXENERGY,
                "Maximum RF storage that the builder can hold").getInt();
        BUILDER_RECEIVEPERTICK = cfg.get(CATEGORY_BUILDER, "builderRFPerTick", BUILDER_RECEIVEPERTICK,
                "RF per tick that the builder can receive").getInt();
        builderRfPerOperation = cfg.get(CATEGORY_BUILDER, "builderRfPerOperation", builderRfPerOperation,
                "RF per block operation for the builder when used to build").getInt();
        builderRfPerQuarry = cfg.get(CATEGORY_BUILDER, "builderRfPerQuarry", builderRfPerQuarry,
                "Base RF per block operation for the builder when used as a quarry or voider (actual cost depends on hardness of block)").getInt();
        builderRfPerEntity = cfg.get(CATEGORY_BUILDER, "builderRfPerEntity", builderRfPerEntity,
                "RF per entity move operation for the builder").getInt();
        builderRfPerPlayer = cfg.get(CATEGORY_BUILDER, "builderRfPerPlayer", builderRfPerPlayer,
                "RF per player move operation for the builder").getInt();
        teMode = BuilderTileEntityMode.find(cfg.get(CATEGORY_BUILDER, "tileEntityMode", teMode.getName(),
                "Can Tile Entities be moved? 'forbidden' means never, 'whitelist' means only whitelisted, 'blacklist' means all except blacklisted, 'allowed' means all").getString());
        maxSpaceChamberDimension = cfg.get(CATEGORY_BUILDER, "maxSpaceChamberDimension", maxSpaceChamberDimension,
                "Maximum dimension for the space chamber").getInt();
        dimensionCostFactor = cfg.get(CATEGORY_BUILDER, "dimensionCostFactor", dimensionCostFactor,
                "How much more expensive a move accross dimensions is").getDouble();

        voidShapeCardFactor = cfg.get(CATEGORY_BUILDER, "voidShapeCardFactor", voidShapeCardFactor,
                "The RF per operation of the builder is multiplied with this factor when using the void shape card").getDouble();
        silkquarryShapeCardFactor = cfg.get(CATEGORY_BUILDER, "silkquarryShapeCardFactor", silkquarryShapeCardFactor,
                "The RF per operation of the builder is multiplied with this factor when using the silk quarry shape card").getDouble();
        fortunequarryShapeCardFactor = cfg.get(CATEGORY_BUILDER, "fortunequarryShapeCardFactor", fortunequarryShapeCardFactor,
                "The RF per operation of the builder is multiplied with this factor when using the fortune quarry shape card").getDouble();

        quarryChunkloads = cfg.get(CATEGORY_BUILDER, "quarryChunkloads", quarryChunkloads,
                "If true the quarry will chunkload a chunk at a time. If false the quarry will stop if a chunk is not loaded").getBoolean();
        shapeCardAllowed = cfg.get(CATEGORY_BUILDER, "shapeCardAllowed", shapeCardAllowed,
                "If true we allow shape cards to be crafted. Note that in order to use the quarry system you must also enable this").getBoolean();
        quarryAllowed = cfg.get(CATEGORY_BUILDER, "quarryAllowed", quarryAllowed,
                "If true we allow quarry cards to be crafted").getBoolean();
        clearingQuarryAllowed = cfg.get(CATEGORY_BUILDER, "clearingQuarryAllowed", clearingQuarryAllowed,
                "If true we allow the clearing quarry cards to be crafted (these can be heavier on the server)").getBoolean();

        quarryBaseSpeed = cfg.get(CATEGORY_BUILDER, "quarryBaseSpeed", quarryBaseSpeed,
                "The base speed (number of blocks per tick) of the quarry").getInt();
        quarryInfusionSpeedFactor = cfg.get(CATEGORY_BUILDER, "quarryInfusionSpeedFactor", quarryInfusionSpeedFactor,
                "Multiply the infusion factor with this value and add that to the quarry base speed").getInt();

        maxBuilderOffset = cfg.get(CATEGORY_BUILDER, "maxBuilderOffset", maxBuilderOffset,
                "Maximum offset of the shape when a shape card is used in the builder").getInt();
        maxBuilderDimension = cfg.get(CATEGORY_BUILDER, "maxBuilderDimension", maxBuilderDimension,
                "Maximum dimension of the shape when a shape card is used in the builder").getInt();

        oldSphereCylinderShape = cfg.get(CATEGORY_BUILDER, "oldSphereCylinderShape", oldSphereCylinderShape,
                "If true we go back to the old (wrong) sphere/cylinder calculation for the builder/shield").getBoolean();
    }
}
