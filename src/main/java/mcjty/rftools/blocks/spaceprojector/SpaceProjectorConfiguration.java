package mcjty.rftools.blocks.spaceprojector;

import net.minecraftforge.common.config.Configuration;

public class SpaceProjectorConfiguration {
    public static final String CATEGORY_SPACEPROJECTOR = "spaceProjector";

    public static int CHAMBERCONTROLLER_MAXENERGY = 100000;
    public static int CHAMBERCONTROLLER_RECEIVEPERTICK = 1000;

    public static int SPACEPROJECTOR_MAXENERGY = 100000;
    public static int SPACEPROJECTOR_RECEIVEPERTICK = 1000;

    public static void init(Configuration cfg) {
        CHAMBERCONTROLLER_MAXENERGY = cfg.get(CATEGORY_SPACEPROJECTOR, "chamberControllerMaxRF", CHAMBERCONTROLLER_MAXENERGY,
                "Maximum RF storage that the chamber controller can hold").getInt();
        CHAMBERCONTROLLER_RECEIVEPERTICK = cfg.get(CATEGORY_SPACEPROJECTOR, "chamberControllerRFPerTick", CHAMBERCONTROLLER_RECEIVEPERTICK,
                "RF per tick that the chamber controller can receive").getInt();
        SPACEPROJECTOR_MAXENERGY = cfg.get(CATEGORY_SPACEPROJECTOR, "spaceProjectorMaxRF", SPACEPROJECTOR_MAXENERGY,
                "Maximum RF storage that the space projector can hold").getInt();
        SPACEPROJECTOR_RECEIVEPERTICK = cfg.get(CATEGORY_SPACEPROJECTOR, "spaceProjectorRFPerTick", SPACEPROJECTOR_RECEIVEPERTICK,
                "RF per tick that the space projector can receive").getInt();
    }
}
