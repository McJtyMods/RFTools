package com.mcjty.rftools.blocks.logic;

import com.mcjty.entity.GenericTileEntity;
import com.mcjty.entity.SyncedValue;
import com.mcjty.rftools.blocks.BlockTools;
import net.minecraft.nbt.NBTTagCompound;

public class RedstoneReceiverTileEntity extends GenericTileEntity {

    private SyncedValue<Boolean> redstoneOut = new SyncedValue<Boolean>(false);
    private int channel = -1;

    public RedstoneReceiverTileEntity() {
        registerSyncedObject(redstoneOut);
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();

        if (channel != -1) {
            RedstoneChannels channels = RedstoneChannels.getChannels(worldObj);
            RedstoneChannels.RedstoneChannel ch = channels.getChannel(channel);
            boolean newout = ch.getValue() != 0;

            if (newout != redstoneOut.getValue()) {
                redstoneOut.setValue(newout);
                notifyBlockUpdate();
            }
        }
    }

    @Override
    protected int updateMetaData(int meta) {
        meta = super.updateMetaData(meta);
        Boolean value = redstoneOut.getValue();
        return BlockTools.setRedstoneSignalOut(meta, value == null ? false : value);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        redstoneOut.setValue(tagCompound.getBoolean("rs"));
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        channel = tagCompound.getInteger("channel");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        Boolean value = redstoneOut.getValue();
        tagCompound.setBoolean("rs", value == null ? false : value);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("channel", channel);
    }
}
