package mcjty.rftools.blocks.blockprotector;

import net.minecraftforge.common.config.Configuration;

public class BlockProtectorConfiguration {
    public static final String CATEGORY_BLOCKPROTECTOR = "blockprotector";
    public static boolean enabled = true;
    public static int MAXENERGY = 500000;
    public static int RECEIVEPERTICK = 20000;
    public static int rfPerProtectedBlock = 5;
    public static int rfForHarvestAttempt = 2000;
    public static int rfForExplosionProtection = 10000;
    public static int maxProtectDistance = 10;

    public static void init(Configuration cfg) {
        cfg.addCustomCategoryComment(BlockProtectorConfiguration.CATEGORY_BLOCKPROTECTOR, "Settings for the block protector machine");
        enabled = cfg.get(CATEGORY_BLOCKPROTECTOR, "enabled", enabled, "Whether the block protector should exist").getBoolean();
        rfPerProtectedBlock = cfg.get(CATEGORY_BLOCKPROTECTOR, "rfPerProtectedBlock", rfPerProtectedBlock, "Amount of passive RF/tick used for every protected block").getInt();
        rfForHarvestAttempt = cfg.get(CATEGORY_BLOCKPROTECTOR, "rfForHarvestAttempt", rfForHarvestAttempt, "The RF that is consumed to protect against a single harvest attempt").getInt();
        rfForExplosionProtection = cfg.get(CATEGORY_BLOCKPROTECTOR, "rfForExplosionProtection", rfForExplosionProtection, "The RF that is consumed to protect a block right next to the explosion with a radius of 8 (standard TNT). Further distances will reduce power usage, bigger radius will increase power usage.").getInt();
        maxProtectDistance = cfg.get(CATEGORY_BLOCKPROTECTOR, "maxProtectDistance", maxProtectDistance, "The maximum distance between the protector and the protected blocks (on one axis)").getInt();
        MAXENERGY = cfg.get(CATEGORY_BLOCKPROTECTOR, "blockProtectorMaxRF", MAXENERGY,
                "Maximum RF storage that the block protector can hold").getInt();
        RECEIVEPERTICK = cfg.get(CATEGORY_BLOCKPROTECTOR, "blockProtectorRFPerTick", RECEIVEPERTICK,
                "RF per tick that the block protector can receive").getInt();
    }
}
