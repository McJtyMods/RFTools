package com.mcjty.rftools.blocks.screens.modules;

import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.varia.Coordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class FluidBarScreenModule implements ScreenModule {
    public static final int RFPERTICK = 4;
    private int dim = 0;
    private Coordinate coordinate = Coordinate.INVALID;

    @Override
    public String getData() {
        World world = RfToolsDimensionManager.getWorldForDimension(dim);
        if (world == null) {
            return null;
        }
        TileEntity te = world.getTileEntity(coordinate.getX(), coordinate.getY(), coordinate.getZ());
        if (!(te instanceof IFluidHandler)) {
            return null;
        }
        IFluidHandler tank = (IFluidHandler) te;
        FluidTankInfo[] tankInfo = tank.getTankInfo(ForgeDirection.DOWN);
        int contents = 0;
        int maxContents = 0;
        if (tankInfo.length > 0 && tankInfo[0].fluid != null) {
            contents = tankInfo[0].fluid.amount;
            maxContents = tankInfo[0].capacity;
        }
        return contents + "/" + maxContents;
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
                    if (dx <= 31 && dy <= 31 && dz <= 31) {
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
