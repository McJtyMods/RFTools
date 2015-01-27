package com.mcjty.rftools.blocks.screens.modules;

import net.minecraft.nbt.NBTTagCompound;

public class TextScreenModule implements ScreenModule {

    public static final int RFPERTICK = 0;

    @Override
    public String getData(long millis) {
        return null;
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {

    }

    @Override
    public int getRfPerTick() {
        return RFPERTICK;
    }
}
