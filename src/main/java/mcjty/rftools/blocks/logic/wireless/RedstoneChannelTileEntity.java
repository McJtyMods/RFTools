package mcjty.rftools.blocks.logic.wireless;

import mcjty.lib.container.LogicTileEntity;
import net.minecraft.nbt.NBTTagCompound;

public abstract class RedstoneChannelTileEntity extends LogicTileEntity {

    protected int channel = -1;

    public int getChannel(boolean initialize) {
        if(initialize && channel == -1) {
            RedstoneChannels redstoneChannels = RedstoneChannels.getChannels(world);
            setChannel(redstoneChannels.newChannel());
            redstoneChannels.save(world);
        }
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
        markDirtyClient();
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        channel = tagCompound.getInteger("channel");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("channel", channel);
    }
}
