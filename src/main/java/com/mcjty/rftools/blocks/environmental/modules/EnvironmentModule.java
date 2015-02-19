package com.mcjty.rftools.blocks.environmental.modules;

import net.minecraft.nbt.NBTTagCompound;

public interface EnvironmentModule {
    void setupFromNBT(NBTTagCompound tagCompound);

    float getRfPerTick();
}
