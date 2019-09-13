package mcjty.rftools.blocks.screens.modules;

import mcjty.lib.api.machineinfo.CapabilityMachineInformation;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.WorldTools;
import mcjty.rftools.api.screens.IScreenDataHelper;
import mcjty.rftools.api.screens.IScreenModule;
import mcjty.rftools.api.screens.data.IModuleDataString;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MachineInformationScreenModule implements IScreenModule<IModuleDataString> {
    private int tag;
    protected int dim = 0;
    protected BlockPos coordinate = BlockPosTools.INVALID;

    @Override
    public IModuleDataString getData(IScreenDataHelper helper, World worldObj, long millis) {
        World world = WorldTools.getWorld(dim);
        if (world == null) {
            return null;
        }

        if (!WorldTools.chunkLoaded(world, coordinate)) {
            return null;
        }

        TileEntity te = world.getTileEntity(coordinate);
        if (te == null) {
            return null;
        }
        return te.getCapability(CapabilityMachineInformation.MACHINE_INFORMATION_CAPABILITY).map(h -> {
            String info;
            if (tag < 0 || tag >= h.getTagCount()) {
                info = "[BAD TAG]";
            } else {
                info = h.getData(tag, millis);
            }
            return helper.createString(info);
        }).orElse(null);
    }

    @Override
    public void setupFromNBT(CompoundNBT tagCompound, int dim, BlockPos pos) {
        if (tagCompound != null) {
            coordinate = BlockPosTools.INVALID;
            tag = tagCompound.getInt("monitorTag");
            if (tagCompound.contains("monitorx")) {
                if (tagCompound.contains("monitordim")) {
                    this.dim = tagCompound.getInt("monitordim");
                } else {
                    // Compatibility reasons
                    this.dim = tagCompound.getInt("dim");
                }
                if (dim == this.dim) {
                    BlockPos c = new BlockPos(tagCompound.getInt("monitorx"), tagCompound.getInt("monitory"), tagCompound.getInt("monitorz"));
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
        return ScreenConfiguration.MACHINEINFO_RFPERTICK.get();
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked, PlayerEntity player) {

    }
}
