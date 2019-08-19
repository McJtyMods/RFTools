package mcjty.rftools.blocks.shield;

import net.minecraftforge.common.ForgeConfigSpec;

public class ShieldConfiguration {
    public static final String CATEGORY_SHIELD = "shield";
    public static ForgeConfigSpec.IntValue MAXENERGY;
    public static ForgeConfigSpec.IntValue RECEIVEPERTICK;
    // The amount of rf to use as a base per 10 blocks in the shield.
    public static ForgeConfigSpec.IntValue rfBase;
    // This amount is added for a camo block.
    public static ForgeConfigSpec.IntValue rfCamo;
    // This amount is added for a shield block.
    public static ForgeConfigSpec.IntValue rfShield;
    // The amount of RF to use per entity for a single damage spike.
    public static ForgeConfigSpec.IntValue rfDamage;
    // The amount of RF to use per entity for a single damage spike (used when simulating player style damage).
    public static ForgeConfigSpec.IntValue rfDamagePlayer;
    // The amount of damage to apply to a given entity.
    public static ForgeConfigSpec.DoubleValue damage;
    // Maximum size of a shield in blocks.
    public static ForgeConfigSpec.IntValue maxShieldSize;
    // Maximum shield offset when the shape card is used
    public static ForgeConfigSpec.IntValue maxShieldOffset;
    // Maximum shield dimension when the shape card is used
    public static ForgeConfigSpec.IntValue maxShieldDimension;
    // Maximum distance at which you can add disjoint shield sections to a composed shield
    public static ForgeConfigSpec.IntValue maxDisjointShieldDistance;

    // Amount of dimensional shards needed to do a looting kill
    public static ForgeConfigSpec.IntValue shardsPerLootingKill;
    // Amount of looting that is done then.
    public static ForgeConfigSpec.IntValue lootingKillBonus;

    // Set to true to temporarily remove the shield blocks to make your world loadable again.
    public static ForgeConfigSpec.BooleanValue disableShieldBlocksToUncorruptWorld;

    // If false invisible shield rendering mode is not allowed
    public static ForgeConfigSpec.BooleanValue allowInvisibleShield;

    public static void init(ForgeConfigSpec.Builder SERVER_BUILDER, ForgeConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Settings for the shield system").push(CATEGORY_SHIELD);
        CLIENT_BUILDER.comment("Settings for the shield system").push(CATEGORY_SHIELD);

        MAXENERGY = SERVER_BUILDER
                .comment("Maximum RF storage that the shield block can hold")
                .defineInRange("shieldMaxRF", 200000, 0, Integer.MAX_VALUE);
        RECEIVEPERTICK = SERVER_BUILDER
                .comment("RF per tick that the shield block can receive")
                .defineInRange("shieldRFPerTick", 5000, 0, Integer.MAX_VALUE);
        maxShieldSize = SERVER_BUILDER
                .comment("Maximum size (in blocks) of a tier 1 shield")
                .defineInRange("shieldMaxSize", 256, 0, 1000000);
        maxShieldOffset = SERVER_BUILDER
                .comment("Maximum offset of the shape when a shape card is used")
                .defineInRange("maxShieldOffset", 128, 0, 100000);
        maxShieldDimension = SERVER_BUILDER
                .comment("Maximum dimension of the shape when a shape card is used")
                .defineInRange("maxShieldDimension", 256, 0, 1000000);
        maxDisjointShieldDistance = SERVER_BUILDER
                .comment("Maximum distance at which you can add disjoint shield sections to a composed shield")
                .defineInRange("maxDisjointShieldDistance", 64, 0, 10000);
        rfBase = SERVER_BUILDER
                .comment("Base amount of RF/tick for every 10 blocks in the shield (while active)")
                .defineInRange("shieldRfBase", 8, 0, Integer.MAX_VALUE);
        rfCamo = SERVER_BUILDER
                .comment("RF/tick for every 10 blocks added in case of camo mode")
                .defineInRange("shieldRfCamo", 2, 0, Integer.MAX_VALUE);
        rfShield = SERVER_BUILDER
                .comment("RF/tick for every 10 block addeds in case of shield mode")
                .defineInRange("shieldRfShield", 2, 0, Integer.MAX_VALUE);
        rfDamage = SERVER_BUILDER
                .comment("The amount of RF to consume for a single spike of damage for one entity")
                .defineInRange("shieldRfDamage", 1000, 0, Integer.MAX_VALUE);
        rfDamagePlayer = SERVER_BUILDER
                .comment("The amount of RF to consume for a single spike of damage for one entity (used in case of player-type damage)")
                .defineInRange("shieldRfDamagePlayer", 2000, 0, Integer.MAX_VALUE);
        damage = SERVER_BUILDER
                .comment("The amount of damage to do for a single spike on one entity")
                .defineInRange("shieldDamage", 5.0, 0.0, 1000000000);
        disableShieldBlocksToUncorruptWorld = SERVER_BUILDER
                .comment("Set this to true if you have a corrupted world due to a bad camo block in the shield system. Load your world, remove the offending block from the shield, exit MC and then set this back to false")
                .define("disableShieldBlocksToUncorruptWorld", false);
        allowInvisibleShield = SERVER_BUILDER
                .comment("Set this to false if you don't want invisible shield rendering mode to be possible")
                .define("allowInvisibleShield", true);
        shardsPerLootingKill = SERVER_BUILDER
                .comment("Amount of dimensional shards per looting kill. Remember that this is per block that does damage")
                .defineInRange("shardsPerLootingKill", 2, 0, 256);
        lootingKillBonus = SERVER_BUILDER
                .comment("The looting kill bonus")
                .defineInRange("lootingKillBonus", 3, 0, 256);

        SERVER_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }
}
