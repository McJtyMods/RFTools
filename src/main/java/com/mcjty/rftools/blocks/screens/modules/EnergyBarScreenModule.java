package com.mcjty.rftools.blocks.screens.modules;

import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.varia.Coordinate;
import com.mcjty.varia.EnergyTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class EnergyBarScreenModule implements ScreenModule {
    public static final int RFPERTICK = 4;
    private int dim = 0;
    private Coordinate coordinate = Coordinate.INVALID;
    private ScreenModuleHelper helper = new ScreenModuleHelper();

    @Override
    public String[] getData(long millis) {
        World world = RfToolsDimensionManager.getWorldForDimension(dim);
        if (world == null) {
            return null;
        }
        TileEntity te = world.getTileEntity(coordinate.getX(), coordinate.getY(), coordinate.getZ());
        if (!EnergyTools.isEnergyTE(te)) {
            return null;
        }
        EnergyTools.EnergyLevelMulti energyLevel = EnergyTools.getEnergyLevelMulti(te);
        long energy = energyLevel.getEnergy();
        long maxEnergy = energyLevel.getMaxEnergy();
        return helper.getContentsValue(millis, energy, maxEnergy);
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        if (tagCompound != null) {
            helper.setShowdiff(tagCompound.getBoolean("showdiff"));
            coordinate = Coordinate.INVALID;
            if (tagCompound.hasKey("monitorx")) {
                this.dim = tagCompound.getInteger("dim");
                if (dim == this.dim) {
                    Coordinate c = new Coordinate(tagCompound.getInteger("monitorx"), tagCompound.getInteger("monitory"), tagCompound.getInteger("monitorz"));
                    int dx = Math.abs(c.getX() - x);
                    int dy = Math.abs(c.getY() - y);
                    int dz = Math.abs(c.getZ() - z);
                    if (dx <= 64 && dy <= 64 && dz <= 64) {
                        coordinate = c;
                    }
                }
            }
        }

    }

    @Override
    public int getRfPerTick() {
        return RFPERTICK;
    }
}
