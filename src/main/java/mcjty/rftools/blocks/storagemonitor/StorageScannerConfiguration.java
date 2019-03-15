package mcjty.rftools.blocks.storagemonitor;

import mcjty.lib.thirteen.ConfigSpec;
import net.minecraftforge.common.config.Configuration;

public class StorageScannerConfiguration {
    public static final String CATEGORY_STORAGE_MONITOR = "storagemonitor";
    public static ConfigSpec.IntValue MAXENERGY;
    public static ConfigSpec.IntValue RECEIVEPERTICK;
    public static ConfigSpec.IntValue rfPerRequest;
    public static ConfigSpec.IntValue rfPerInsert;
    public static ConfigSpec.IntValue hilightTime;

    public static ConfigSpec.BooleanValue hilightStarredOnGuiOpen;
    public static ConfigSpec.BooleanValue requestStraightToInventory;

    public static ConfigSpec.BooleanValue xnetRequired;

    public static void init(ConfigSpec.Builder SERVER_BUILDER, ConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Settings for the storage scanner machine").push(CATEGORY_STORAGE_MONITOR);
        CLIENT_BUILDER.comment("Settings for the storage scanner machine").push(CATEGORY_STORAGE_MONITOR);

        rfPerRequest = SERVER_BUILDER
                .comment("Amount of RF used to request an item")
                .defineInRange("rfPerRequest", 100, 0, Integer.MAX_VALUE);
        rfPerInsert = SERVER_BUILDER
                .comment("Amount of RF used to insert an item")
                .defineInRange("rfPerInsert", 20, 0, Integer.MAX_VALUE);
        hilightTime = CLIENT_BUILDER
                .comment("Time (in seconds) to hilight a block in the world")
                .defineInRange("hilightTime", 5, 0, Integer.MAX_VALUE);
        MAXENERGY = SERVER_BUILDER
                .comment("Maximum RF storage that the storage scanner can hold")
                .defineInRange("scannerMaxRF", 50000, 0, Integer.MAX_VALUE);
        RECEIVEPERTICK = SERVER_BUILDER
                .comment("RF per tick that the storage scanner can receive")
                .defineInRange("scannerRFPerTick", 500, 0, Integer.MAX_VALUE);

        hilightStarredOnGuiOpen = CLIENT_BUILDER
                .comment("If this is true then opening the storage scanner GUI will automatically select the starred inventory view")
                .define("hilightStarredOnGuiOpen", true);
        requestStraightToInventory = SERVER_BUILDER
                .comment("If this is true then requesting items from the storage scanner will go straight into the player inventory and not the output slot")
                .define("requestStraightToInventory", true);
        xnetRequired = SERVER_BUILDER
                .comment("If this is true then XNet is required (if present) to be able to connect storages to a storage scanner")
                .define("xnetRequired", false);

        SERVER_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }
}
