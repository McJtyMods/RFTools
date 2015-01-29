package com.mcjty.rftools.blocks.screens.modules;

import com.mcjty.varia.Coordinate;
import net.minecraft.nbt.NBTTagCompound;

public class EnergyPlusBarScreenModule extends EnergyBarScreenModule {
    public static final int RFPERTICK = 30;

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        if (tagCompound != null) {
            helper.setShowdiff(tagCompound.getBoolean("showdiff"));
            coordinate = Coordinate.INVALID;
            if (tagCompound.hasKey("monitorx")) {
                this.dim = tagCompound.getInteger("dim");
                coordinate = new Coordinate(tagCompound.getInteger("monitorx"), tagCompound.getInteger("monitory"), tagCompound.getInteger("monitorz"));
            }
        }
    }

    @Override
    public int getRfPerTick() {
        return RFPERTICK;
    }
}
