package com.mcjty.rftools.blocks.environmental.modules;

import net.minecraft.potion.Potion;

public class SpeedPlusEModule extends PotionEffectModule {
    public static final float RFPERTICK = 0.02f;

    public SpeedPlusEModule() {
        super(Potion.moveSpeed.getId(), 2);
    }

    @Override
    public float getRfPerTick() {
        return RFPERTICK;
    }
}
