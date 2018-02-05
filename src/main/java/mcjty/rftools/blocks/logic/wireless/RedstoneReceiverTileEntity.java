package mcjty.rftools.blocks.logic.wireless;

import mcjty.rftools.blocks.logic.generic.LogicTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

public class RedstoneReceiverTileEntity extends LogicTileEntity implements ITickable {

    private int channel = -1;

    public RedstoneReceiverTileEntity() {
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
        markDirtyClient();
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            checkStateServer();
        }
    }

    protected void checkStateServer() {
        setRedstoneState(checkOutput() ? 15 : 0);
    }

    public boolean checkOutput() {
        boolean newout = false;
        if (channel != -1) {
            RedstoneChannels channels = RedstoneChannels.getChannels(getWorld());
            RedstoneChannels.RedstoneChannel ch = channels.getChannel(channel);
            if (ch != null) {
                newout = ch.getValue() != 0;
            }
        }
        return newout;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        powerOutput = tagCompound.getBoolean("rs") ? 15 : 0;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        channel = tagCompound.getInteger("channel");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("rs", powerOutput > 0);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("channel", channel);
    }
}
