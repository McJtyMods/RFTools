package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.entity.GenericTileEntity;
import com.mcjty.entity.SyncedValue;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.dimension.DimensionStorage;
import com.mcjty.rftools.network.Argument;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public class DimensionMonitorTileEntity extends GenericTileEntity {

    public static final String CMD_SETALARM = "setAlarm";

    private int alarmLevel = 0;
    private int ticker = 10;
    private SyncedValue<Boolean> redstoneOut = new SyncedValue<Boolean>(false);

    public DimensionMonitorTileEntity() {
        registerSyncedObject(redstoneOut);
    }

    public int getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(int level) {
        this.alarmLevel = level;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();

        ticker--;
        if (ticker > 0) {
            return;
        }
        ticker = 10;

        DimensionStorage storage = DimensionStorage.getDimensionStorage(worldObj);
        int energy = storage.getEnergyLevel(worldObj.provider.dimensionId);

        int pct = energy / (DimletConfiguration.MAX_DIMENSION_POWER / 100);
        boolean newout = pct < alarmLevel;

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
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        alarmLevel = tagCompound.getInteger("level");
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
        tagCompound.setInteger("level", alarmLevel);
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETALARM.equals(command)) {
            setAlarmLevel(args.get("level").getInteger());
            return true;
        }
        return false;
    }
}
