package mcjty.rftools.blocks.logic.wireless;

import mcjty.lib.tileentity.LogicTileEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public abstract class RedstoneChannelTileEntity extends LogicTileEntity {

    protected int channel = -1;

    public int getChannel(boolean initialize) {
        if(initialize && channel == -1) {
            RedstoneChannels redstoneChannels = RedstoneChannels.getChannels(world);
            setChannel(redstoneChannels.newChannel());
            redstoneChannels.save();
        }
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
        markDirtyClient();
    }

    @Override
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        channel = tagCompound.getInteger("channel");
    }

    @Override
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("channel", channel);
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, BlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        int channel = getChannel(false);
        if(channel == -1) {
            probeInfo.text(TextFormatting.YELLOW + "No channel set! Right-click with another");
            probeInfo.text(TextFormatting.YELLOW + "transmitter or receiver to pair");
        } else {
            probeInfo.text(TextFormatting.GREEN + "Channel: " + channel);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);
        int channel = getChannel(false);
        if(channel == -1) {
            currenttip.add(TextFormatting.YELLOW + "No channel set! Right-click with another");
            currenttip.add(TextFormatting.YELLOW + "transmitter or receiver to pair");
        } else {
            currenttip.add(TextFormatting.GREEN + "Channel: " + channel);
        }
    }


}
