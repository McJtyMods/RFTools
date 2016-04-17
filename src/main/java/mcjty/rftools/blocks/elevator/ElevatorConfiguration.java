package mcjty.rftools.blocks.elevator;

import net.minecraftforge.common.config.Configuration;

public class ElevatorConfiguration {

    public static final String CATEGORY_ELEVATOR = "elevator";
    public static int MAXENERGY = 50000;
    public static int RFPERTICK = 500;
    public static int rfPerTickMoving = 60;

    public static void init(Configuration cfg) {
        rfPerTickMoving = cfg.get(CATEGORY_ELEVATOR, "elevatorPerTick", rfPerTickMoving, "Amount of RF used per tick when moving").getInt();
        MAXENERGY = cfg.get(CATEGORY_ELEVATOR, "elevatorMaxRF", MAXENERGY,
                "Maximum RF storage that the generator can hold").getInt();
        RFPERTICK = cfg.get(CATEGORY_ELEVATOR, "elevatorRFPerTick", RFPERTICK,
                              "RF per tick that the evelator block can receive").getInt();
    }
}
