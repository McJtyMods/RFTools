package com.mcjty.rftools.blocks.shield;

import net.minecraftforge.common.config.Configuration;

public class ShieldConfiguration {
    public static final String CATEGORY_SHIELD = "Shield";
    public static int MAXENERGY = 100000;
    public static int RECEIVEPERTICK = 1000;
    // The amount of rf to use as a base per block in the shield.
    public static int rfBase = 8;
    // This amount is added for a camo block.
    public static int rfCamo = 2;
    // This amount is added for a shield block.
    public static int rfShield = 2;
    // The amount of RF to use per entity for a single damage spike.
    public static int rfDamage = 1000;
    // The amount of damage to apply to a given entity.
    public static float damage = 5.0f;
    // Maximum size of a shield in blocks.
    public static int maxShieldSize = 256;

    public static void init(Configuration cfg) {
        MAXENERGY = cfg.get(CATEGORY_SHIELD, "shieldMaxRF", MAXENERGY,
                "Maximum RF storage that the shield block can hold").getInt();
        RECEIVEPERTICK = cfg.get(CATEGORY_SHIELD, "shieldRFPerTick", RECEIVEPERTICK,
                "RF per tick that the shield block can receive").getInt();
        maxShieldSize = cfg.get(CATEGORY_SHIELD, "shieldMaxSize", maxShieldSize,
                "Maximum size (in blocks) of a shield").getInt();
        rfBase = cfg.get(CATEGORY_SHIELD, "shieldRfBase", rfBase,
                "Base amount of RF/tick for every block in the shield (while active)").getInt();
        rfCamo = cfg.get(CATEGORY_SHIELD, "shieldRfCamo", rfCamo,
                "RF/tick for every block added in case of camo mode").getInt();
        rfShield = cfg.get(CATEGORY_SHIELD, "shieldRfShield", rfShield,
                "RF/tick for every block added in case of shield mode").getInt();
        rfDamage = cfg.get(CATEGORY_SHIELD, "shieldRfDamage", rfDamage,
                "The amount of RF to consume for a single spike of damage for one entity").getInt();
        damage = (float) cfg.get(CATEGORY_SHIELD, "shieldDamage", damage,
                "The amount of damage to do for a single spike on one entity").getDouble();
    }
}
