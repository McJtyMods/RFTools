package mcjty.rftools.blocks.logic.wireless;

import mcjty.lib.tileentity.LogicTileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;

public abstract class RedstoneChannelTileEntity extends LogicTileEntity {

    protected int channel = -1;

    public RedstoneChannelTileEntity(TileEntityType<?> type) {
        super(type);
    }

    public int getChannel(boolean initialize) {
        if(initialize && channel == -1) {
            RedstoneChannels redstoneChannels = RedstoneChannels.get();
            setChannel(redstoneChannels.newChannel());
            redstoneChannels.save();
        }
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
        markDirtyClient();
    }

    // @todo 1.14 loot tables
    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        channel = tagCompound.getInt("channel");
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        tagCompound.putInt("channel", channel);
        return tagCompound;
    }

//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        int channel = getChannel(false);
//        if(channel == -1) {
//            probeInfo.text(TextFormatting.YELLOW + "No channel set! Right-click with another");
//            probeInfo.text(TextFormatting.YELLOW + "transmitter or receiver to pair");
//        } else {
//            probeInfo.text(TextFormatting.GREEN + "Channel: " + channel);
//        }
//    }
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    @Optional.Method(modid = "waila")
//    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.addWailaBody(itemStack, currenttip, accessor, config);
//        int channel = getChannel(false);
//        if(channel == -1) {
//            currenttip.add(TextFormatting.YELLOW + "No channel set! Right-click with another");
//            currenttip.add(TextFormatting.YELLOW + "transmitter or receiver to pair");
//        } else {
//            currenttip.add(TextFormatting.GREEN + "Channel: " + channel);
//        }
//    }

}
