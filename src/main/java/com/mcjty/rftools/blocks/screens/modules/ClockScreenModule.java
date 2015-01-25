package com.mcjty.rftools.blocks.screens.modules;

import net.minecraft.nbt.NBTTagCompound;

public class ClockScreenModule implements ScreenModule {
    @Override
    public String getData() {
        return null;
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {

    }

    @Override
    public int getRfPerTick() {
        return 1;
    }
}
