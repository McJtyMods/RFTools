package mcjty.rftools.blocks.elevator;

import mcjty.lib.thirteen.ConfigSpec;

public class ElevatorConfiguration {

    public static final String CATEGORY_ELEVATOR = "elevator";
    public static ConfigSpec.IntValue MAXENERGY;
    public static ConfigSpec.IntValue RFPERTICK;
    public static ConfigSpec.IntValue rfPerHeightUnit;
    public static ConfigSpec.IntValue maxPlatformSize;
    public static ConfigSpec.DoubleValue minimumSpeed;
    public static ConfigSpec.DoubleValue maximumSpeed;
    public static ConfigSpec.DoubleValue maxSpeedDistanceStart;
    public static ConfigSpec.DoubleValue maxSpeedDistanceEnd;

    public static ConfigSpec.DoubleValue baseElevatorVolume;      // Use 0 to turn off elevator sounds
    public static ConfigSpec.DoubleValue loopVolumeFactor;        // How much to decrease volume of the looping sound.

    public static void init(ConfigSpec.Builder SERVER_BUILDER, ConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Settings for the elevator").push(CATEGORY_ELEVATOR);
        CLIENT_BUILDER.comment("Settings for the elevator").push(CATEGORY_ELEVATOR);

        rfPerHeightUnit = SERVER_BUILDER
                .comment("Amount of RF used per height level when moving")
                .defineInRange("rfPerHeightUnit", 500, 0, Integer.MAX_VALUE);
        maxPlatformSize = SERVER_BUILDER
                .comment("Maximum platform size that can be moved")
                .defineInRange("maxPlatformSize", 11, 0, 10000);
        minimumSpeed = SERVER_BUILDER
                .comment("Mimumum elevator speed")
                .defineInRange("minimumSpeed", .1, 0.0, 1000000000.0);
        maximumSpeed = SERVER_BUILDER
                .comment("Maximum elevator speed")
                .defineInRange("maximumSpeed", .3, 0.0, 1000000000.0);
        maxSpeedDistanceStart = SERVER_BUILDER
                .comment("Distance from the start at which maximum speed is reached")
                .defineInRange("maxSpeedDistanceStart", 5, 0.0, 1000000000.0);
        maxSpeedDistanceEnd = SERVER_BUILDER
                .comment("Distance from the end at which speed will start going down again")
                .defineInRange("maxSpeedDistanceEnd", 2, 0.0, 1000000000.0);
        MAXENERGY = SERVER_BUILDER
                .comment("Maximum RF storage that the generator can hold")
                .defineInRange("elevatorMaximumRF", 150000, 0, Integer.MAX_VALUE);
        RFPERTICK = SERVER_BUILDER
                .comment("RF per tick that the evelator block can receive")
                .defineInRange("elevatorRFPerTick", 1000, 0, Integer.MAX_VALUE);

        baseElevatorVolume = CLIENT_BUILDER
                .comment("The volume for the elevator sound (1.0 is default, 0.0 is off)")
                .defineInRange("baseElevatorVolume", 1.0, 0.0, 1.0);
        loopVolumeFactor = CLIENT_BUILDER
                .comment("Relative volume of the elevator looping sound. With 1.0 the looping sound has equal loudness as the elevator base volume")
                .defineInRange("loopVolumeFactor", 1.0, 0.0, 1.0);

        SERVER_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }
}
