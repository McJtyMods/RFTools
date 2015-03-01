package com.mcjty.rftools.blocks.environmental.modules;

import com.mcjty.rftools.PlayerBuff;
import net.minecraft.potion.Potion;

public class SpeedEModule extends PotionEffectModule {
    public static final float RFPERTICK = 0.001f;

    public SpeedEModule() {
        super(Potion.moveSpeed.getId(), 0);
    }

    @Override
    public float getRfPerTick() {
        return RFPERTICK;
    }

    @Override
    protected PlayerBuff getBuff() {
        return PlayerBuff.BUFF_SPEED;
    }
}
