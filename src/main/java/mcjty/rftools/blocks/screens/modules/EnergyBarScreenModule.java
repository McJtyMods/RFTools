package mcjty.rftools.blocks.screens.modules;

import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.EnergyTools;
import mcjty.lib.varia.WorldTools;
import mcjty.rftools.api.screens.IScreenDataHelper;
import mcjty.rftools.api.screens.IScreenModule;
import mcjty.rftools.api.screens.data.IModuleDataContents;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class EnergyBarScreenModule implements IScreenModule<IModuleDataContents> {
    protected int dim = 0;
    protected BlockPos coordinate = BlockPosTools.INVALID;
    protected EnumFacing side = EnumFacing.DOWN;
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

        TileEntity te = world.getTileEntity(coordinate);
        if (!EnergyTools.isEnergyTE(te, side)) {
            return null;
        }
        EnergyTools.EnergyLevel energyLevel = EnergyTools.getEnergyLevelMulti(te, side);
        long energy = energyLevel.getEnergy();
        long maxEnergy = energyLevel.getMaxEnergy();
        return helper.getContentsValue(millis, energy, maxEnergy);
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
                        if(tagCompound.hasKey("monitorside")) {
                            side = EnumFacing.VALUES[tagCompound.getInteger("monitorside")];
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getRfPerTick() {
        return ScreenConfiguration.ENERGY_RFPERTICK.get();
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked, EntityPlayer player) {

    }
}
