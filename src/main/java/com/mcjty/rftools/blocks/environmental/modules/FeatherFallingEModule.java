package com.mcjty.rftools.blocks.environmental.modules;

import com.mcjty.rftools.PlayerBuff;

public class FeatherFallingEModule extends BuffEModule {
    public static final float RFPERTICK = 0.001f;

    public FeatherFallingEModule() {
        super(PlayerBuff.BUFF_FEATHERFALLING);
    }

    @Override
    public float getRfPerTick() {
        return RFPERTICK;
    }
}
