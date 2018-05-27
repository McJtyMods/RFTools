package mcjty.rftools.blocks.endergen;

import net.minecraftforge.common.config.Configuration;

public class EndergenicConfiguration {
    public static final String CATEGORY_ENDERGENIC = "endergenic";
    // This value indicates the chance (with 0 being no chance and 1000 being 100% chance) that an
    // endergenic pearl is lost while holding it.
    public static int chanceLost = 5;
    // This value indicates how much RF is being consumed every tick to try to keep the endergenic pearl.
    public static long rfToHoldPearl = 500;
    // This value indicates how much RF will be kept in the internal buffer (not given to conduits and machines next to it) as
    // a reserve to be able to hold pearls.
    public static long keepRfInBuffer = 2000;
    // This value indicates how much RF/tick this block can send out to neighbours
    public static long rfOutput = 20000;
    public static int goodParticleCount = 10;
    public static int badParticleCount = 10;

    public static double powergenFactor = 2.0;

    public static void init(Configuration cfg) {
        chanceLost = cfg.get(CATEGORY_ENDERGENIC, "endergenicChanceLost", chanceLost,
                "The chance (in 1/10 percent, so 1000 = 100%) that an endergenic pearl is lost while trying to hold it").getInt();
        rfToHoldPearl = cfg.get(CATEGORY_ENDERGENIC, "endergenicRfHolding", rfToHoldPearl,
                "The amount of RF that is consumed every tick to hold the endergenic pearl").getLong();
        keepRfInBuffer = cfg.get(CATEGORY_ENDERGENIC, "endergenicKeepRf", keepRfInBuffer,
                "The amount of RF that every endergenic will keep itself (so that it can hold pearls)").getLong();
        rfOutput = cfg.get(CATEGORY_ENDERGENIC, "endergenicRfOutput", rfOutput,
                "The amount of RF per tick that this generator can give from its internal buffer to adjacent blocks").getLong();
        goodParticleCount = cfg.get(CATEGORY_ENDERGENIC, "endergenicGoodParticles", goodParticleCount,
                "The amount of particles to spawn whenever energy is generated (use 0 to disable)").getInt();
        badParticleCount = cfg.get(CATEGORY_ENDERGENIC, "endergenicBadParticles", badParticleCount,
                "The amount of particles to spawn whenever a pearl is lost (use 0 to disable)").getInt();
        powergenFactor = cfg.get(CATEGORY_ENDERGENIC, "powergenFactor", powergenFactor,
                "Multiplier for power generation").getDouble();
    }
}
