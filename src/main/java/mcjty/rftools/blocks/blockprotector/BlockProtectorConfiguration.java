package mcjty.rftools.blocks.blockprotector;

import net.minecraftforge.common.config.Configuration;

public class BlockProtectorConfiguration {
    public static final String CATEGORY_BLOCKPROTECTOR = "blockprotector";
    public static int MAXENERGY = 500000;
    public static int RECEIVEPERTICK = 20000;
    public static int rfPerProtectedBlock = 5;
    public static int rfForHarvestAttempt = 100;
    public static int rfForExplosionProtection = 500;

    public static void init(Configuration cfg) {
        rfPerProtectedBlock = cfg.get(CATEGORY_BLOCKPROTECTOR, "rfPerProtectedBlock", rfPerProtectedBlock, "Amount of passive RF/tick used for every protected block").getInt();
        rfForHarvestAttempt = cfg.get(CATEGORY_BLOCKPROTECTOR, "rfForHarvestAttempt", rfForHarvestAttempt, "The RF that is consumed to protect against a single harvest attempt").getInt();
        rfForExplosionProtection = cfg.get(CATEGORY_BLOCKPROTECTOR, "rfForExplosionProtection", rfForExplosionProtection, "The RF that is consumed to protect against a single explosion. This is the value that is used for blocks adjacent to the explosion").getInt();
        MAXENERGY = cfg.get(CATEGORY_BLOCKPROTECTOR, "blockProtectorMaxRF", MAXENERGY,
                "Maximum RF storage that the block protector can hold").getInt();
        RECEIVEPERTICK = cfg.get(CATEGORY_BLOCKPROTECTOR, "blockProtectorRFPerTick", RECEIVEPERTICK,
                "RF per tick that the block protector can receive").getInt();
    }
}
