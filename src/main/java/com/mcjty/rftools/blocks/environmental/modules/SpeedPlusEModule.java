package com.mcjty.rftools.blocks.environmental.modules;

import com.mcjty.rftools.PlayerBuff;
import net.minecraft.potion.Potion;

public class SpeedPlusEModule extends PotionEffectModule {
    public static final float RFPERTICK = 0.003f;

    public SpeedPlusEModule() {
        super(Potion.moveSpeed.getId(), 2);
    }

    @Override
    public float getRfPerTick() {
        return RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_SPEEDPLUS;
    }
}
