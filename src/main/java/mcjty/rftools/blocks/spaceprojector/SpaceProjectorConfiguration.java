package mcjty.rftools.blocks.spaceprojector;

import net.minecraftforge.common.config.Configuration;

public class SpaceProjectorConfiguration {
    public static final String CATEGORY_SPACEPROJECTOR = "spaceProjector";

    public static int SPACEPROJECTOR_MAXENERGY = 100000;
    public static int SPACEPROJECTOR_RECEIVEPERTICK = 1000;

    public static int BUILDER_MAXENERGY = 10000000;
    public static int BUILDER_RECEIVEPERTICK = 50000;

    public static int builderRfPerOperation = 500;
    public static int builderRfPerEntity = 5000;
    public static int builderRfPerPlayer = 40000;
    public static double dimensionCostFactor = 5.0f;

    public static BuilderTileEntityMode teMode = BuilderTileEntityMode.MOVE_WHITELIST;

    public static int maxSpaceChamberDimension = 128;

    public static double voidShapeCardFactor = 0.5;
    public static double quarryShapeCardFactor = 2.0;
    public static double silkquarryShapeCardFactor = 4.0;
    public static double fortunequarryShapeCardFactor = 3.0;
    public static boolean quarryChunkloads = true;

    public static void init(Configuration cfg) {
        SPACEPROJECTOR_MAXENERGY = cfg.get(CATEGORY_SPACEPROJECTOR, "spaceProjectorMaxRF", SPACEPROJECTOR_MAXENERGY,
                "Maximum RF storage that the space projector can hold").getInt();
        SPACEPROJECTOR_RECEIVEPERTICK = cfg.get(CATEGORY_SPACEPROJECTOR, "spaceProjectorRFPerTick", SPACEPROJECTOR_RECEIVEPERTICK,
                "RF per tick that the space projector can receive").getInt();
        BUILDER_MAXENERGY = cfg.get(CATEGORY_SPACEPROJECTOR, "builderMaxRF", BUILDER_MAXENERGY,
                "Maximum RF storage that the builder can hold").getInt();
        BUILDER_RECEIVEPERTICK = cfg.get(CATEGORY_SPACEPROJECTOR, "builderRFPerTick", BUILDER_RECEIVEPERTICK,
                "RF per tick that the builder can receive").getInt();
        builderRfPerOperation = cfg.get(CATEGORY_SPACEPROJECTOR, "builderRfPerOperation", builderRfPerOperation,
                "RF per block operation for the builder").getInt();
        builderRfPerEntity = cfg.get(CATEGORY_SPACEPROJECTOR, "builderRfPerEntity", builderRfPerEntity,
                "RF per entity move operation for the builder").getInt();
        builderRfPerPlayer = cfg.get(CATEGORY_SPACEPROJECTOR, "builderRfPerPlayer", builderRfPerPlayer,
                "RF per player move operation for the builder").getInt();
        teMode = BuilderTileEntityMode.find(cfg.get(CATEGORY_SPACEPROJECTOR, "tileEntityMode", teMode.getName(),
                "Can Tile Entities be moved? 'forbidden' means never, 'whitelist' means only whitelisted, 'blacklist' means all except blacklisted, 'allowed' means all").getString());
        maxSpaceChamberDimension = cfg.get(CATEGORY_SPACEPROJECTOR, "maxSpaceChamberDimension", maxSpaceChamberDimension,
                "Maximum dimension for the space chamber").getInt();
        dimensionCostFactor = cfg.get(CATEGORY_SPACEPROJECTOR, "dimensionCostFactor", dimensionCostFactor,
                "How much more expensive a move accross dimensions is").getDouble();

        voidShapeCardFactor = cfg.get(CATEGORY_SPACEPROJECTOR, "voidShapeCardFactor", voidShapeCardFactor,
                "The RF per operation of the builder is multiplied with this factor when using the void shape card").getDouble();
        quarryShapeCardFactor = cfg.get(CATEGORY_SPACEPROJECTOR, "quarryShapeCardFactor", quarryShapeCardFactor,
                "The RF per operation of the builder is multiplied with this factor when using the quarry shape card").getDouble();
        silkquarryShapeCardFactor = cfg.get(CATEGORY_SPACEPROJECTOR, "silkquarryShapeCardFactor", silkquarryShapeCardFactor,
                "The RF per operation of the builder is multiplied with this factor when using the silk quarry shape card").getDouble();
        fortunequarryShapeCardFactor = cfg.get(CATEGORY_SPACEPROJECTOR, "fortunequarryShapeCardFactor", fortunequarryShapeCardFactor,
                "The RF per operation of the builder is multiplied with this factor when using the fortune quarry shape card").getDouble();

        quarryChunkloads = cfg.get(CATEGORY_SPACEPROJECTOR, "quarryChunkloads", quarryChunkloads,
                "If true the quarry will chunkload a chunk at a time. If false the quarry will stop if a chunk is not loaded").getBoolean();
    }
}
