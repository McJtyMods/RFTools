package com.mcjty.rftools.blocks.environmental.modules;

import net.minecraft.potion.Potion;

public class HasteEModule extends PotionEffectModule {
    public static final float RFPERTICK = 0.004f;

    public HasteEModule() {
        super(Potion.digSpeed.getId(), 0);
    }

    @Override
    public float getRfPerTick() {
        return RFPERTICK;
    }
}
