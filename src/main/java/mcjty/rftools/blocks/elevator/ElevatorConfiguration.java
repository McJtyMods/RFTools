package mcjty.rftools.blocks.elevator;

import net.minecraftforge.common.config.Configuration;

public class ElevatorConfiguration {

    public static final String CATEGORY_ELEVATOR = "elevator";
    public static int MAXENERGY = 50000;
    public static int RFPERTICK = 500;
    public static int rfPerTickMoving = 60;
    public static int maxPlatformSize = 11;
    public static double minimumSpeed = .1;
    public static double maximumSpeed = .3;
    public static double maxSpeedDistanceStart = 5;
    public static double maxSpeedDistanceEnd = 2;

    public static void init(Configuration cfg) {
        rfPerTickMoving = cfg.get(CATEGORY_ELEVATOR, "elevatorPerTick", rfPerTickMoving, "Amount of RF used per tick when moving").getInt();
        maxPlatformSize = cfg.get(CATEGORY_ELEVATOR, "maxPlatformSize", maxPlatformSize, "Maximum platform size that can be moved").getInt();
        minimumSpeed = cfg.get(CATEGORY_ELEVATOR, "minimumSpeed", minimumSpeed, "Mimumum elevator speed").getDouble();
        maximumSpeed = cfg.get(CATEGORY_ELEVATOR, "maximumSpeed", maximumSpeed, "Maximum elevator speed").getDouble();
        maxSpeedDistanceStart = cfg.get(CATEGORY_ELEVATOR, "maxSpeedDistanceStart", maxSpeedDistanceStart, "Distance from the start at which maximum speed is reached").getDouble();
        maxSpeedDistanceEnd = cfg.get(CATEGORY_ELEVATOR, "maxSpeedDistanceEnd", maxSpeedDistanceEnd, "Distance from the end at which speed will start going down again").getDouble();
        MAXENERGY = cfg.get(CATEGORY_ELEVATOR, "elevatorMaxRF", MAXENERGY,
                "Maximum RF storage that the generator can hold").getInt();
        RFPERTICK = cfg.get(CATEGORY_ELEVATOR, "elevatorRFPerTick", RFPERTICK,
                              "RF per tick that the evelator block can receive").getInt();
    }
}
