package mcjty.rftools.blocks.screens.modules;

import mcjty.lib.varia.Coordinate;
import mcjty.rftools.blocks.logic.RedstoneChannels;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class RedstoneScreenModule implements ScreenModule {
    private int channel = -1;
    private Coordinate coordinate = Coordinate.INVALID;
    private int dim = 0;
    private int side = 0;

    @Override
    public Object[] getData(World worldObj, long millis) {
        if (channel == -1) {
            // If we are monitoring some block then we can use that.
            if (coordinate.isValid()) {
                World world = DimensionManager.getWorld(dim);
                if (world != null) {
//                    int powerTo = world.isBlockProvidingPowerTo(coordinate.getX(), coordinate.getY(), coordinate.getZ(), side);
                    int powerTo = world.getIndirectPowerLevelTo(coordinate.getX(), coordinate.getY(), coordinate.getZ(), side);

                    return new Object[] { powerTo > 0 ? true : false };
                }
            }
            return null;
        }
        RedstoneChannels channels = RedstoneChannels.getChannels();
        if (channels == null) {
            return null;
        }
        RedstoneChannels.RedstoneChannel ch = channels.getChannel(channel);
        if (ch == null) {
            return null;
        }
        return new Object[] { ch.getValue() != 0 ? true : false };
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        if (tagCompound != null) {
            channel = -1;
            if (tagCompound.hasKey("channel")) {
                channel = tagCompound.getInteger("channel");
            }
            if (tagCompound.hasKey("monitorx")) {
                this.side = tagCompound.getInteger("monitorside");
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
        return ScreenConfiguration.REDSTONE_RFPERTICK;
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }
}
