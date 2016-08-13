package mcjty.rftools.blocks.logic.wireless;

import mcjty.rftools.blocks.logic.generic.LogicTileEntity;
import net.minecraft.nbt.NBTTagCompound;

public class RedstoneTransmitterTileEntity extends LogicTileEntity {

    private int channel = -1;
    private boolean prevIn = false;

    public RedstoneTransmitterTileEntity() {
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
        markDirtyClient();
    }

    public void update() {
        if (worldObj.isRemote) {
            return;
        }

        if (channel == -1) {
            return;
        }

        boolean powered = powerLevel > 0;
        if (powered != prevIn) {
            prevIn = powered;
            markDirty();
            RedstoneChannels channels = RedstoneChannels.getChannels(worldObj);
            RedstoneChannels.RedstoneChannel ch = channels.getOrCreateChannel(channel);
            ch.setValue(powered ? 15 : 0);
            channels.save(worldObj);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        prevIn = tagCompound.getBoolean("prevIn");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("prevIn", prevIn);
        return tagCompound;
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
