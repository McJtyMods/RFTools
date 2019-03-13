package mcjty.rftools.blocks.crafter;

import net.minecraftforge.common.config.Configuration;

public class CrafterConfiguration {
    public static final String CATEGORY_CRAFTER = "crafter";
    public static boolean enabled = true;
    public static int MAXENERGY = 50000;
    public static int RECEIVEPERTICK = 500;
    public static int rfPerOperation = 100;
    public static int speedOperations = 5;

    public static void init(Configuration cfg) {
        cfg.addCustomCategoryComment(CrafterConfiguration.CATEGORY_CRAFTER, "Settings for the crafter");
        enabled = cfg.get(CATEGORY_CRAFTER, "enabled", enabled, "Whether the crafter should exist").getBoolean();
        rfPerOperation = cfg.get(CATEGORY_CRAFTER, "rfPerOperation", rfPerOperation, "Amount of RF used per crafting operation").getInt();
        speedOperations = cfg.get(CATEGORY_CRAFTER, "speedOperations", speedOperations, "How many operations to do at once in fast mode").getInt();
        MAXENERGY = cfg.get(CATEGORY_CRAFTER, "crafterMaxRF", MAXENERGY,
                "Maximum RF storage that the crafter can hold").getInt();
        RECEIVEPERTICK = cfg.get(CATEGORY_CRAFTER, "crafterRFPerTick", RECEIVEPERTICK,
                "RF per tick that the crafter can receive").getInt();
    }
}
