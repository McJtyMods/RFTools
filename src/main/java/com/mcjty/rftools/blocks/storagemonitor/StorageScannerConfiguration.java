package com.mcjty.rftools.blocks.storagemonitor;

import net.minecraftforge.common.config.Configuration;

public class StorageScannerConfiguration {
    public static final String CATEGORY_STORAGE_MONITOR = "storagemonitor";
    public static int MAXENERGY = 100000;
    public static int RECEIVEPERTICK = 500;
    public static int rfPerOperation = 100;
    public static int scansPerOperation = 10;
    public static int hilightTime = 5;

    public static void init(Configuration cfg) {
        rfPerOperation = cfg.get(CATEGORY_STORAGE_MONITOR, "rfPerOperation", rfPerOperation, "Amount of RF used per scan operation").getInt();
        scansPerOperation = cfg.get(CATEGORY_STORAGE_MONITOR, "scansPerOperation", scansPerOperation, "How many blocks to scan per operation").getInt();
        hilightTime = cfg.get(CATEGORY_STORAGE_MONITOR, "hilightTime", hilightTime, "Time (in seconds) to hilight a block in the world").getInt();
        MAXENERGY = cfg.get(CATEGORY_STORAGE_MONITOR, "scannerMaxRF", MAXENERGY,
                "Maximum RF storage that the storage scanner can hold").getInt();
        RECEIVEPERTICK = cfg.get(CATEGORY_STORAGE_MONITOR, "scannerRFPerTick", RECEIVEPERTICK,
                "RF per tick that the storage scanner can receive").getInt();
    }
}
