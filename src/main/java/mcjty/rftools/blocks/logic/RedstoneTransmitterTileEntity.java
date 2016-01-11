package mcjty.rftools.blocks.logic;

import mcjty.lib.entity.GenericTileEntity;
import net.minecraft.nbt.NBTTagCompound;

public class RedstoneTransmitterTileEntity extends GenericTileEntity {

    private int channel = -1;
    private boolean prevValue = false;
    private boolean powered = false;

    public RedstoneTransmitterTileEntity() {
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
        markDirtyClient();
    }

    @Override
    public void setPowered(int powered) {
        this.powered = powered > 0;
    }

    public void update() {
        if (worldObj.isRemote) {
            return;
        }

        if (channel == -1) {
            return;
        }

        if (powered != prevValue) {
            prevValue = powered;
            RedstoneChannels channels = RedstoneChannels.getChannels(worldObj);
            RedstoneChannels.RedstoneChannel ch = channels.getOrCreateChannel(channel);
            ch.setValue(powered ? 15 : 0);
            channels.save(worldObj);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        powered = tagCompound.getBoolean("powered");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        channel = tagCompound.getInteger("channel");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("powered", powered);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("channel", channel);
    }
}
