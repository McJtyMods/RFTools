package com.mcjty.rftools.blocks.environmental.modules;

import com.mcjty.rftools.PlayerBuff;
import net.minecraft.potion.Potion;

public class SaturationPlusEModule extends PotionEffectModule {
    public static final float RFPERTICK = 0.003f;

    public SaturationPlusEModule() {
        super(Potion.field_76443_y.getId(), 2);
    }

    @Override
    public float getRfPerTick() {
        return RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_SATURATIONPLUS;
    }
}
