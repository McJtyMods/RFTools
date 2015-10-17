package mcjty.rftools.blocks.screens.modules;

import mcjty.lib.varia.Coordinate;
import mcjty.rftools.blocks.logic.CounterTileEntity;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class CounterScreenModule implements ScreenModule {
    protected int dim = 0;
    protected Coordinate coordinate = Coordinate.INVALID;

    @Override
    public Object[] getData(World worldObj, long millis) {
        World world = DimensionManager.getWorld(dim);
        if (world == null) {
            return null;
        }

        if (!world.getChunkProvider().chunkExists(coordinate.getX() >> 4, coordinate.getZ() >> 4)) {
            return null;
        }

        TileEntity te = world.getTileEntity(coordinate.getX(), coordinate.getY(), coordinate.getZ());

        if (!(te instanceof CounterTileEntity)) {
            return null;
        }

        CounterTileEntity counterTileEntity = (CounterTileEntity) te;
        return new Object[] { counterTileEntity.getCurrent() };
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
                    if (dx <= 64 && dy <= 64 && dz <= 64) {
                        coordinate = c;
                    }
                }
            }
        }
    }

    @Override
    public int getRfPerTick() {
        return ScreenConfiguration.COUNTER_RFPERTICK;
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }
}
