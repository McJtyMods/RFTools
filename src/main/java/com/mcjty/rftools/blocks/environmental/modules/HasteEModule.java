package com.mcjty.rftools.blocks.environmental.modules;

import com.mcjty.rftools.PlayerBuff;
import net.minecraft.potion.Potion;

public class HasteEModule extends PotionEffectModule {
    public static final float RFPERTICK = 0.001f;

    public HasteEModule() {
        super(Potion.digSpeed.getId(), 0);
    }

    @Override
    public float getRfPerTick() {
        return RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_HASTE;
    }
}
