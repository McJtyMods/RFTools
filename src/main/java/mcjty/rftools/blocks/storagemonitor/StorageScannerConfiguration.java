package mcjty.rftools.blocks.storagemonitor;

import net.minecraftforge.common.config.Configuration;

public class StorageScannerConfiguration {
    public static final String CATEGORY_STORAGE_MONITOR = "storagemonitor";
    public static int MAXENERGY = 50000;
    public static int RECEIVEPERTICK = 500;
    public static int rfPerRequest = 100;
    public static int rfPerInsert = 20;
    public static int hilightTime = 5;

    public static boolean hilightStarredOnGuiOpen = true;
    public static boolean requestStraightToInventory = true;

    public static void init(Configuration cfg) {
        rfPerRequest = cfg.get(CATEGORY_STORAGE_MONITOR, "rfPerRequest", rfPerRequest, "Amount of RF used to request an item").getInt();
        rfPerInsert = cfg.get(CATEGORY_STORAGE_MONITOR, "rfPerInsert", rfPerInsert, "Amount of RF used to insert an item").getInt();
        hilightTime = cfg.get(CATEGORY_STORAGE_MONITOR, "hilightTime", hilightTime, "Time (in seconds) to hilight a block in the world").getInt();
        MAXENERGY = cfg.get(CATEGORY_STORAGE_MONITOR, "scannerMaxRF", MAXENERGY,
                "Maximum RF storage that the storage scanner can hold").getInt();
        RECEIVEPERTICK = cfg.get(CATEGORY_STORAGE_MONITOR, "scannerRFPerTick", RECEIVEPERTICK,
                "RF per tick that the storage scanner can receive").getInt();
        hilightStarredOnGuiOpen = cfg.get(CATEGORY_STORAGE_MONITOR, "hilightStarredOnGuiOpen", hilightStarredOnGuiOpen, "If this is true then opening the storage scanner GUI will automatically select the starred inventory view").getBoolean();
        requestStraightToInventory = cfg.get(CATEGORY_STORAGE_MONITOR, "requestStraightToInventory", requestStraightToInventory, "If this is true then requesting items from the storage scanner will go straight into the player inventory and not the output slot").getBoolean();
    }
}
