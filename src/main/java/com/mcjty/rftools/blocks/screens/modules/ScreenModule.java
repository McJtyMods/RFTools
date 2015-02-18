package com.mcjty.rftools.blocks.screens.modules;

import net.minecraft.nbt.NBTTagCompound;

public interface ScreenModule {
    Object[] getData(long millis);

    void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z);

    int getRfPerTick();
}
