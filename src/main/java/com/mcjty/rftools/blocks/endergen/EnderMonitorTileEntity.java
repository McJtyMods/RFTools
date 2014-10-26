package com.mcjty.rftools.blocks.endergen;

import com.mcjty.entity.GenericTileEntity;
import com.mcjty.entity.SyncedValue;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.network.Argument;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public class EnderMonitorTileEntity extends GenericTileEntity {

    public static final int MODE_LOSTPEARL = 0;
    public static final int MODE_PEARLFIRED = 1;
    public static final int MODE_PEARLARRIVED = 2;

    public static final String CMD_MODE = "mode";

    private int mode = MODE_LOSTPEARL;

    private SyncedValue<Boolean> redstoneOut = new SyncedValue<Boolean>(false);

    public EnderMonitorTileEntity() {
        registerSyncedObject(redstoneOut);
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();

        markDirty();

        boolean newout = true;


        if (newout != redstoneOut.getValue()) {
            redstoneOut.setValue(newout);
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
        redstoneOut.setValue(tagCompound.getBoolean("rs"));
        mode = tagCompound.getInteger("mode");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        Boolean value = redstoneOut.getValue();
        tagCompound.setBoolean("rs", value == null ? false : value);
        tagCompound.setInteger("mode", mode);
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
        if (rc) {
            return true;
        }
        if (CMD_MODE.equals(command)) {
            setMode(args.get("mode").getInteger());
            return true;
        }
        return false;
    }

}
