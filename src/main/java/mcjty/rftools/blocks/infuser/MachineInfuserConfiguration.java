package mcjty.rftools.blocks.infuser;

import mcjty.lib.thirteen.ConfigSpec;

public class MachineInfuserConfiguration {

    public static final String CATEGORY_INFUSER = "infuser";

    public static ConfigSpec.IntValue MAXENERGY;
    public static ConfigSpec.IntValue RECEIVEPERTICK;
    public static ConfigSpec.IntValue rfPerTick;

    public static void init(ConfigSpec.Builder SERVER_BUILDER, ConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Settings for the infuser").push(CATEGORY_INFUSER);
        CLIENT_BUILDER.comment("Settings for the infuser").push(CATEGORY_INFUSER);

        rfPerTick = SERVER_BUILDER
                .comment("Amount of RF used per tick while infusing")
                .defineInRange("generatePerTick", 600, 0, Integer.MAX_VALUE);
        MAXENERGY = SERVER_BUILDER
                .comment("Maximum RF storage that the infuser can hold")
                .defineInRange("infuserMaxRF", 60000, 0, Integer.MAX_VALUE);
        RECEIVEPERTICK = SERVER_BUILDER
                .comment("RF per tick that the infuser can receive")
                .defineInRange("infuserRFPerTick", 600, 0, Integer.MAX_VALUE);

        SERVER_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }
}
