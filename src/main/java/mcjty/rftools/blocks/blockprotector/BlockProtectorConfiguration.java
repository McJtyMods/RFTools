package mcjty.rftools.blocks.blockprotector;

import net.minecraftforge.common.ForgeConfigSpec;

public class BlockProtectorConfiguration {
    public static final String CATEGORY_BLOCKPROTECTOR = "blockprotector";

    public static ForgeConfigSpec.BooleanValue enabled;

    public static ForgeConfigSpec.IntValue MAXENERGY;
    public static ForgeConfigSpec.IntValue RECEIVEPERTICK;
    public static ForgeConfigSpec.IntValue rfPerProtectedBlock;
    public static ForgeConfigSpec.IntValue rfForHarvestAttempt;
    public static ForgeConfigSpec.IntValue rfForExplosionProtection;
    public static ForgeConfigSpec.IntValue maxProtectDistance;

    public static void init(ForgeConfigSpec.Builder SERVER_BUILDER, ForgeConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Settings for the block protector machine").push(CATEGORY_BLOCKPROTECTOR);
        CLIENT_BUILDER.comment("Settings for the block protector machine").push(CATEGORY_BLOCKPROTECTOR);

        enabled = SERVER_BUILDER
                .comment("Whether the block protector should exist")
                .define("enabled", true);
        rfPerProtectedBlock = SERVER_BUILDER
                .comment("Amount of passive RF/tick used for every protected block")
                .defineInRange("rfPerProtectedBlock", 5, 0, Integer.MAX_VALUE);
        rfForHarvestAttempt = SERVER_BUILDER
                .comment("The RF that is consumed to protect against a single harvest attempt")
                .defineInRange("rfForHarvestAttempt", 2000, 0, Integer.MAX_VALUE);
        rfForExplosionProtection = SERVER_BUILDER
                .comment("The RF that is consumed to protect a block right next to the explosion with a radius of 8 (standard TNT). Further distances will reduce power usage, bigger radius will increase power usage.")
                .defineInRange("rfForExplosionProtection", 10000, 0, Integer.MAX_VALUE);
        maxProtectDistance = SERVER_BUILDER
                .comment("The maximum distance between the protector and the protected blocks (on one axis)")
                .defineInRange("maxProtectDistance", 10, 0, 1000000);
        MAXENERGY = SERVER_BUILDER
                .comment("Maximum RF storage that the block protector can hold")
                .defineInRange("blockProtectorMaxRF", 500000, 0, Integer.MAX_VALUE);
        RECEIVEPERTICK = SERVER_BUILDER
                .comment("RF per tick that the block protector can receive")
                .defineInRange("blockProtectorRFPerTick", 20000, 0, Integer.MAX_VALUE);

        SERVER_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }
}
