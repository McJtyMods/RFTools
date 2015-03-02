package com.mcjty.rftools.blocks.environmental.modules;

import com.mcjty.rftools.PlayerBuff;

public class FeatherFallingPlusEModule extends BuffEModule {
    public static final float RFPERTICK = 0.003f;

    public FeatherFallingPlusEModule() {
        super(PlayerBuff.BUFF_FEATHERFALLINGPLUS);
    }

    @Override
    public float getRfPerTick() {
        return RFPERTICK;
    }
}
