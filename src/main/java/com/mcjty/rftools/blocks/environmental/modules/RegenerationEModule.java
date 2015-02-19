package com.mcjty.rftools.blocks.environmental.modules;

import net.minecraft.nbt.NBTTagCompound;

public class RegenerationEModule implements EnvironmentModule {

    public static final float RFPERTICK = 0.001f;


    @Override
    public void setupFromNBT(NBTTagCompound tagCompound) {

    }

    @Override
    public float getRfPerTick() {
        return RFPERTICK;
    }
}
