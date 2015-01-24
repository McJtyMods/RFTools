package com.mcjty.rftools.blocks.screens.modules;

import cofh.api.energy.IEnergyHandler;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.varia.Coordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class EnergyBarScreenModule implements ScreenModule {
    private int dim = 0;
    private Coordinate coordinate = Coordinate.INVALID;

    @Override
    public String getData() {
        World world = RfToolsDimensionManager.getWorldForDimension(dim);
        if (world == null) {
            return null;
        }
        TileEntity te = world.getTileEntity(coordinate.getX(), coordinate.getY(), coordinate.getZ());
        if (!(te instanceof IEnergyHandler)) {
            return null;
        }
        int energy = ((IEnergyHandler)te).getEnergyStored(ForgeDirection.DOWN);
        int maxEnergy = ((IEnergyHandler)te).getMaxEnergyStored(ForgeDirection.DOWN);
        return energy + "/" + maxEnergy;
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        if (tagCompound != null) {
            coordinate = Coordinate.INVALID;
            if (tagCompound.hasKey("monitorx")) {
                this.dim = tagCompound.getInteger("dim");
                if (dim == this.dim) {
                    Coordinate c = new Coordinate(tagCompound.getInteger("monitorx"), tagCompound.getInteger("monitory"), tagCompound.getInteger("monitorz"));
                    int dx = Math.abs(c.getX() - x);
                    int dy = Math.abs(c.getY() - y);
                    int dz = Math.abs(c.getZ() - z);
                    if (dx <= 16 && dy <= 16 && dz <= 16) {
                        coordinate = c;
                    }
                }
            }
        }

    }
}
