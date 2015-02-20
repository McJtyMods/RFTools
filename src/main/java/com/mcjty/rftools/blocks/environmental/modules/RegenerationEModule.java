package com.mcjty.rftools.blocks.environmental.modules;

import net.minecraft.potion.Potion;

public class RegenerationEModule extends PotionEffectModule {
    public static final float RFPERTICK = 0.006f;

    public RegenerationEModule() {
        super(Potion.regeneration.getId(), 0);
    }

    @Override
    public float getRfPerTick() {
        return RFPERTICK;
    }
}
