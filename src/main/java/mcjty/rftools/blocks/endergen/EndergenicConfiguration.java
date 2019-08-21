package mcjty.rftools.blocks.endergen;

import net.minecraftforge.common.ForgeConfigSpec;

public class EndergenicConfiguration {
    public static final String CATEGORY_ENDERGENIC = "endergenic";
    // This value indicates the chance (with 0 being no chance and 1000 being 100% chance) that an
    // endergenic pearl is lost while holding it.
    public static ForgeConfigSpec.IntValue chanceLost;
    // This value indicates how much RF is being consumed every tick to try to keep the endergenic pearl.
    public static ForgeConfigSpec.IntValue rfToHoldPearl;
    // This value indicates how much RF will be kept in the internal buffer (not given to conduits and machines next to it) as
    // a reserve to be able to hold pearls.
    public static ForgeConfigSpec.IntValue keepRfInBuffer;
    // This value indicates how much RF/tick this block can send out to neighbours
    public static ForgeConfigSpec.IntValue rfOutput;
    public static ForgeConfigSpec.IntValue goodParticleCount;
    public static ForgeConfigSpec.IntValue badParticleCount;

    public static ForgeConfigSpec.DoubleValue powergenFactor;

    public static void init(ForgeConfigSpec.Builder SERVER_BUILDER, ForgeConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Settings for the endergenic generator").push(CATEGORY_ENDERGENIC);
        CLIENT_BUILDER.comment("Settings for the endergenic generator").push(CATEGORY_ENDERGENIC);

        chanceLost = SERVER_BUILDER
                .comment("The chance (in 1/10 percent, so 1000 = 100%) that an endergenic pearl is lost while trying to hold it")
                .defineInRange("endergenicChanceLost", 5, 0, 1000);
        rfToHoldPearl = SERVER_BUILDER
                .comment("The amount of RF that is consumed every tick to hold the endergenic pearl")
                .defineInRange("endergenicRfHolding", 500, 0, Integer.MAX_VALUE);
        keepRfInBuffer = SERVER_BUILDER
                .comment("The amount of RF that every endergenic will keep itself (so that it can hold pearls)")
                .defineInRange("endergenicKeepRf", 2000, 0, Integer.MAX_VALUE);
        rfOutput = SERVER_BUILDER
                .comment("The amount of RF per tick that this generator can give from its internal buffer to adjacent blocks")
                .defineInRange("endergenicRfOutput", 20000, 0, Integer.MAX_VALUE);
        goodParticleCount = SERVER_BUILDER
                .comment("The amount of particles to spawn whenever energy is generated (use 0 to disable)")
                .defineInRange("endergenicGoodParticles", 10, 0, 1000);
        badParticleCount = SERVER_BUILDER
                .comment("The amount of particles to spawn whenever a pearl is lost (use 0 to disable)")
                .defineInRange("endergenicBadParticles", 10, 0, 1000);
        powergenFactor = SERVER_BUILDER
                .comment("Multiplier for power generation")
                .defineInRange("powergenFactor", 2.0, 0, 1000000000.0);

        SERVER_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }
}
