package mcjty.rftools.blocks.spaceprojector;

import net.minecraftforge.common.config.Configuration;

public class SpaceProjectorConfiguration {
    public static final String CATEGORY_SPACEPROJECTOR = "spaceProjector";

    public static int CHAMBERCONTROLLER_MAXENERGY = 100000;
    public static int CHAMBERCONTROLLER_RECEIVEPERTICK = 1000;

    public static int SPACEPROJECTOR_MAXENERGY = 100000;
    public static int SPACEPROJECTOR_RECEIVEPERTICK = 1000;

    public static int BUILDER_MAXENERGY = 500000;
    public static int BUILDER_RECEIVEPERTICK = 5000;

    public static int builderRfPerOperation = 500;
    public static boolean ignoreTileEntities = false;

    public static void init(Configuration cfg) {
        CHAMBERCONTROLLER_MAXENERGY = cfg.get(CATEGORY_SPACEPROJECTOR, "chamberControllerMaxRF", CHAMBERCONTROLLER_MAXENERGY,
                "Maximum RF storage that the chamber controller can hold").getInt();
        CHAMBERCONTROLLER_RECEIVEPERTICK = cfg.get(CATEGORY_SPACEPROJECTOR, "chamberControllerRFPerTick", CHAMBERCONTROLLER_RECEIVEPERTICK,
                "RF per tick that the chamber controller can receive").getInt();
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
        ignoreTileEntities = cfg.get(CATEGORY_SPACEPROJECTOR, "ignoreTileEntities", ignoreTileEntities,
                "If false then the builder will move/swap tile entities. Otherwise it will leave these blocks alone").getBoolean();
    }
}
