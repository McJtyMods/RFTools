package com.mcjty.rftools.blocks.logic;

import com.mcjty.entity.GenericTileEntity;
import com.mcjty.entity.SyncedValue;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.network.Argument;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public class TimerTileEntity extends GenericTileEntity {

    // For pulse detection.
    private boolean prevIn = false;

    private SyncedValue<Boolean> redstoneOut = new SyncedValue<Boolean>(false);

    public TimerTileEntity() {
        registerSyncedObject(redstoneOut);
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();

        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        boolean newvalue = BlockTools.getRedstoneSignalIn(meta);
        boolean pulse = newvalue && !prevIn;
        prevIn = newvalue;

        if (pulse) {
//            handlePulse();
        }

//        markDirty();
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
        prevIn = tagCompound.getBoolean("prevIn");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        Boolean value = redstoneOut.getValue();
        tagCompound.setBoolean("rs", value == null ? false : value);
        tagCompound.setBoolean("prevIn", prevIn);
    }

//    @Override
//    public boolean execute(String command, Map<String, Argument> args) {
//        boolean rc = super.execute(command, args);
//        if (rc) {
//            return true;
//        }
//        if (CMD_MODE.equals(command)) {
//            setMode(args.get("mode").getInteger());
//            return true;
//        } else if (CMD_SETBIT.equals(command)) {
//            setCycleBit(args.get("bit").getInteger(), args.get("choice").getBoolean());
//            return true;
//        } else if (CMD_SETBITS.equals(command)) {
//            setCycleBits(args.get("start").getInteger(), args.get("stop").getInteger(), args.get("choice").getBoolean());
//            return true;
//        } else if (CMD_SETDELAY.equals(command)) {
//            setDelay(args.get("delay").getInteger());
//            return true;
//        }
//        return false;
//    }
}
