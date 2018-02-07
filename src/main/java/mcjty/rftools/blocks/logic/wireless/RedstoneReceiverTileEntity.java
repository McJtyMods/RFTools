package mcjty.rftools.blocks.logic.wireless;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

public class RedstoneReceiverTileEntity extends RedstoneChannelTileEntity implements ITickable {

    public RedstoneReceiverTileEntity() {
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            checkStateServer();
        }
    }

    protected void checkStateServer() {
        setRedstoneState(checkOutput());
    }

    public int checkOutput() {
        int newout = 0;
        if (channel != -1) {
            RedstoneChannels channels = RedstoneChannels.getChannels(getWorld());
            RedstoneChannels.RedstoneChannel ch = channels.getChannel(channel);
            if (ch != null) {
                newout = ch.getValue();
            }
        }
        return newout;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        if(tagCompound.hasKey("rs", 3 /* int */)) {
            powerOutput = tagCompound.getInteger("rs");
        } else {
            powerOutput = tagCompound.getBoolean("rs") ? 15 : 0; // backwards compatibility
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("rs", powerOutput);
        return tagCompound;
    }
}
