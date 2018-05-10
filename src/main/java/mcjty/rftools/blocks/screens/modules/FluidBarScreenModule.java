package mcjty.rftools.blocks.screens.modules;

import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.CapabilityTools;
import mcjty.lib.varia.WorldTools;
import mcjty.rftools.api.screens.IScreenDataHelper;
import mcjty.rftools.api.screens.IScreenModule;
import mcjty.rftools.api.screens.data.IModuleDataContents;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.varia.RFToolsTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class FluidBarScreenModule implements IScreenModule<IModuleDataContents> {
    protected int dim = 0;
    protected BlockPos coordinate = BlockPosTools.INVALID;
    protected ScreenModuleHelper helper = new ScreenModuleHelper();

    @Override
    public IModuleDataContents getData(IScreenDataHelper h, World worldObj, long millis) {
        World world = DimensionManager.getWorld(dim);
        if (world == null) {
            return null;
        }

        if (!WorldTools.chunkLoaded(world, coordinate)) {
            return null;
        }

        int contents = 0;
        int maxContents = 0;

        TileEntity te = world.getTileEntity(coordinate);
        net.minecraftforge.fluids.capability.IFluidHandler handler = CapabilityTools.hasFluidCapabilitySafe(te);
        if (handler != null) {
            IFluidTankProperties[] properties = handler.getTankProperties();
            if (properties != null && properties.length > 0) {
                if (properties[0].getContents() != null) {
                    contents = properties[0].getContents().amount;
                }
                maxContents = properties[0].getCapacity();
            }
        } else {
            return null;
        }

        return helper.getContentsValue(millis, contents, maxContents);
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, BlockPos pos) {
        if (tagCompound != null) {
            helper.setShowdiff(tagCompound.getBoolean("showdiff"));
            coordinate = BlockPosTools.INVALID;
            if (tagCompound.hasKey("monitorx")) {
                if (tagCompound.hasKey("monitordim")) {
                    this.dim = tagCompound.getInteger("monitordim");
                } else {
                    // Compatibility reasons
                    this.dim = tagCompound.getInteger("dim");
                }
                if (dim == this.dim) {
                    BlockPos c = new BlockPos(tagCompound.getInteger("monitorx"), tagCompound.getInteger("monitory"), tagCompound.getInteger("monitorz"));
                    int dx = Math.abs(c.getX() - pos.getX());
                    int dy = Math.abs(c.getY() - pos.getY());
                    int dz = Math.abs(c.getZ() - pos.getZ());
                    if (dx <= 64 && dy <= 64 && dz <= 64) {
                        coordinate = c;
                    }
                }
            }
        }

    }

    @Override
    public int getRfPerTick() {
        return ScreenConfiguration.FLUID_RFPERTICK;
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked, EntityPlayer player) {

    }
}
