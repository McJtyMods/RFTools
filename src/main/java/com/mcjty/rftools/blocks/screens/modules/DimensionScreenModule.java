package com.mcjty.rftools.blocks.screens.modules;

import com.mcjty.rftools.dimension.DimensionStorage;
import com.mcjty.varia.Coordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.DimensionManager;

public class DimensionScreenModule implements ScreenModule {
    private int dim = 0;

    @Override
    public String getData() {
        int energy = DimensionStorage.getDimensionStorage(DimensionManager.getWorld(0)).getEnergyLevel(dim);
        return Integer.toString(energy);
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        if (tagCompound != null) {
            this.dim = tagCompound.getInteger("dim");
        }
    }
}
