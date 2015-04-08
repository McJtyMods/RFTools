package mcjty.rftools.blocks.logic;

import mcjty.entity.GenericTileEntity;
import mcjty.rftools.blocks.BlockTools;
import net.minecraft.nbt.NBTTagCompound;

public class RedstoneTransmitterTileEntity extends GenericTileEntity {

    private int channel = -1;
    private int prevValue = -1;

    public RedstoneTransmitterTileEntity() {
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        markDirty();
    }

    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();

        if (channel == -1) {
            return;
        }

        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        int newvalue = BlockTools.getRedstoneSignalIn(meta) ? 1 : 0;
        if (newvalue != prevValue) {
            prevValue = newvalue;
            RedstoneChannels channels = RedstoneChannels.getChannels(worldObj);
            RedstoneChannels.RedstoneChannel ch = channels.getOrCreateChannel(channel);
            ch.setValue(newvalue == 1 ? 15 : 0);
            channels.save(worldObj);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        channel = tagCompound.getInteger("channel");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("channel", channel);
    }
}
