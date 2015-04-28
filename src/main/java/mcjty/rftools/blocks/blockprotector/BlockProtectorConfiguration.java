package mcjty.rftools.blocks.blockprotector;

import net.minecraftforge.common.config.Configuration;

public class BlockProtectorConfiguration {
    public static final String CATEGORY_BLOCKPROTECTOR = "blockprotector";
    public static int MAXENERGY = 200000;
    public static int RECEIVEPERTICK = 10000;
    public static int rfPerProtectedBlock = 20;

    public static void init(Configuration cfg) {
        rfPerProtectedBlock = cfg.get(CATEGORY_BLOCKPROTECTOR, "rfPerProtectedBlock", rfPerProtectedBlock, "Amount of RF used for every protected block").getInt();
        MAXENERGY = cfg.get(CATEGORY_BLOCKPROTECTOR, "blockProtectorMaxRF", MAXENERGY,
                "Maximum RF storage that the block protector can hold").getInt();
        RECEIVEPERTICK = cfg.get(CATEGORY_BLOCKPROTECTOR, "blockProtectorRFPerTick", RECEIVEPERTICK,
                "RF per tick that the block protector can receive").getInt();
    }
}
