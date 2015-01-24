package com.mcjty.rftools.blocks.screens.modules;

import net.minecraft.nbt.NBTTagCompound;

public interface ScreenModule {
    String getData();

    void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z);
}
