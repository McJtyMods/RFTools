package com.mcjty.rftools.blocks.environmental.modules;

import net.minecraft.potion.Potion;

public class SaturationEModule extends PotionEffectModule {
    public static final float RFPERTICK = 0.001f;

    public SaturationEModule() {
        super(Potion.field_76443_y.getId(), 0);
    }

    @Override
    public float getRfPerTick() {
        return RFPERTICK;
    }
}
