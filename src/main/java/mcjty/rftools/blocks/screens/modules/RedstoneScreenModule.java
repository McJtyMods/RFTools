package mcjty.rftools.blocks.screens.modules;

import mcjty.lib.varia.BlockPosTools;
import mcjty.rftools.api.screens.IScreenDataHelper;
import mcjty.rftools.api.screens.IScreenModule;
import mcjty.rftools.api.screens.data.IModuleDataInteger;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class RedstoneScreenModule implements IScreenModule<IModuleDataInteger> {
    private int channel = -1;
    private BlockPos coordinate = BlockPosTools.INVALID;
    private int dim = 0;
    private Direction side = null;

    @Override
    public IModuleDataInteger getData(IScreenDataHelper helper, World worldObj, long millis) {
        if (channel == -1) {
            // If we are monitoring some block then we can use that.
            if (!BlockPosTools.INVALID.equals(coordinate)) {
                World world = DimensionManager.getWorld(dim);
                if (world != null) {
//                    int powerTo = world.isBlockProvidingPowerTo(coordinate.getX(), coordinate.getY(), coordinate.getZ(), side);
                    int powerTo = world.getRedstonePower(coordinate.offset(side), side.getOpposite());
//                    int powerTo = world.getIndirectPowerLevelTo(coordinate.getX(), coordinate.getY(), coordinate.getZ(), side);

                    return helper.createInteger(powerTo);
                }
            }
            return null;
        }
        RedstoneChannels channels = RedstoneChannels.getChannels(worldObj);
        if (channels == null) {
            return null;
        }
        RedstoneChannels.RedstoneChannel ch = channels.getChannel(channel);
        if (ch == null) {
            return null;
        }
        return helper.createInteger(ch.getValue());
    }

    @Override
    public void setupFromNBT(CompoundNBT tagCompound, int dim, BlockPos pos) {
        if (tagCompound != null) {
            channel = -1;
            if (tagCompound.hasKey("channel")) {
                channel = tagCompound.getInt("channel");
            }
            if (tagCompound.hasKey("monitorx")) {
                side = Direction.VALUES[tagCompound.getInt("monitorside")];
                if (tagCompound.hasKey("monitordim")) {
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
        return ScreenConfiguration.REDSTONE_RFPERTICK.get();
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked, PlayerEntity player) {

    }
}
