package com.mcjty.rftools.blocks.environmental.modules;

import net.minecraft.potion.Potion;

public class SaturationPlusEModule extends PotionEffectModule {
    public static final float RFPERTICK = 0.01f;

    public SaturationPlusEModule() {
        super(Potion.field_76443_y.getId(), 2);
    }

    @Override
    public float getRfPerTick() {
        return RFPERTICK;
    }
}
