package com.mcjty.rftools.blocks.environmental.modules;

import net.minecraft.potion.Potion;

public class HastePlusEModule extends PotionEffectModule {
    public static final float RFPERTICK = 0.02f;

    public HastePlusEModule() {
        super(Potion.digSpeed.getId(), 2);
    }

    @Override
    public float getRfPerTick() {
        return RFPERTICK;
    }
}
