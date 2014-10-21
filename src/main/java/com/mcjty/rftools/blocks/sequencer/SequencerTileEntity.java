package com.mcjty.rftools.blocks.sequencer;

import com.mcjty.entity.GenericTileEntity;
import com.mcjty.entity.SyncedValue;
import com.mcjty.rftools.blocks.BlockTools;
import net.minecraft.nbt.NBTTagCompound;

public class SequencerTileEntity extends GenericTileEntity {

    private int cycle = 0;
    private SyncedValue<Boolean> redstoneOut = new SyncedValue<Boolean>(false);

    public SequencerTileEntity() {
        registerSyncedObject(redstoneOut);
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();

        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        boolean newvalue;
        if (BlockTools.getRedstoneSignalIn(meta)) {
            newvalue = true;
        } else {
            newvalue = false;
        }
        if (newvalue != redstoneOut.getValue()) {
            redstoneOut.setValue(newvalue);
            notifyBlockUpdate();
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
        cycle = tagCompound.getInteger("cycle");
        redstoneOut.setValue(tagCompound.getBoolean("rs"));
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("cycle", cycle);
        Boolean value = redstoneOut.getValue();
        tagCompound.setBoolean("rs", value == null ? false : value);

    }
}
