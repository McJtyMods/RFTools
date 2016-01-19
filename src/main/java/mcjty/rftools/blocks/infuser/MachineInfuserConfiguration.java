package mcjty.rftools.blocks.infuser;

import net.minecraftforge.common.config.Configuration;

public class MachineInfuserConfiguration {

    public static final String CATEGORY_INFUSER = "infuser";
    public static int MAXENERGY = 60000;
    public static int RECEIVEPERTICK = 600;
    public static int rfPerTick = 600;

    public static void init(Configuration cfg) {
        rfPerTick = cfg.get(CATEGORY_INFUSER, "generatePerTick", rfPerTick, "Amount of RF used per tick while infusing").getInt();
        MAXENERGY = cfg.get(CATEGORY_INFUSER, "infuserMaxRF", MAXENERGY,
                "Maximum RF storage that the infuser can hold").getInt();
        RECEIVEPERTICK = cfg.get(CATEGORY_INFUSER, "infuserRFPerTick", RECEIVEPERTICK,
                              "RF per tick that the infuser can receive").getInt();
    }
}
