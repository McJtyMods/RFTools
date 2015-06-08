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
    public static boolean ignoreTileEntities = false;

    public static int maxSpaceChamberDimension = 128;

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
                "RF per entity operation for the builder").getInt();
        ignoreTileEntities = cfg.get(CATEGORY_SPACEPROJECTOR, "ignoreTileEntities", ignoreTileEntities,
                "If false then the builder will move/swap tile entities. Otherwise it will leave these blocks alone").getBoolean();
        maxSpaceChamberDimension = cfg.get(CATEGORY_SPACEPROJECTOR, "maxSpaceChamberDimension", maxSpaceChamberDimension,
                "Maximum dimension for the space chamber").getInt();
    }
}
