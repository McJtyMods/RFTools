package com.mcjty.rftools.blocks.monitor;

import cofh.api.energy.IEnergyHandler;
import com.mcjty.entity.GenericTileEntity;
import com.mcjty.entity.SyncedValue;
import com.mcjty.rftools.Coordinate;
import com.mcjty.rftools.blocks.BlockTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class RFMonitorBlockTileEntity extends GenericTileEntity {
    // Data that is saved
    private int monitorX = -1;
    private int monitorY = -1;  // Invalid y coordinate so we know it is not initialized yet
    private int monitorZ = -1;
    private RFMonitorMode alarmMode = RFMonitorMode.MODE_OFF;
    private int alarmLevel = 0;             // The level (in percentage) at which we give an alarm

    // Temporary data
    private int counter = 20;
    private SyncedValue<Integer> rflevel = new SyncedValue<Integer>(0);
    private SyncedValue<Boolean> inAlarm = new SyncedValue<Boolean>(false);

    public RFMonitorBlockTileEntity() {
        registerSyncedValue(rflevel);
        registerSyncedValue(inAlarm);
    }

    public RFMonitorMode getAlarmMode() {
        return alarmMode;
    }

    public int getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarm(RFMonitorMode mode, int level) {
        alarmMode = mode;
        alarmLevel = level;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public int getMonitorX() {
        return monitorX;
    }

    public int getMonitorY() {
        return monitorY;
    }

    public int getMonitorZ() {
        return monitorZ;
    }

    public boolean isValid() {
        return monitorY >= 0;
    }

    @Override
    public void setInvalid() {
        monitorX = -1;
        monitorY = -1;
        monitorZ = -1;
        super.setInvalid();
    }

    public void setMonitor(Coordinate c) {
        monitorX = c.getX();
        monitorY = c.getY();
        monitorZ = c.getZ();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public int getRflevel() {
        return rflevel.getValue();
    }

    @Override
    protected int updateMetaData(int meta) {
        meta = super.updateMetaData(meta);
        return BlockTools.setRedstoneSignal(meta, inAlarm.getValue());
    }

    @Override
    protected void checkStateServer() {
        if (!isValid()) {
            counter = 1;
            return;
        }

        counter--;
        if (counter > 0) {
            return;
        }
        counter = 20;

        TileEntity tileEntity = worldObj.getTileEntity(monitorX, monitorY, monitorZ);
        if (!(tileEntity instanceof IEnergyHandler)) {
            setInvalid();
            return;
        }
        IEnergyHandler handler = (IEnergyHandler) tileEntity;
        int maxEnergy = handler.getMaxEnergyStored(ForgeDirection.DOWN);
        int ratio = 0;  // Will be set as metadata;
        boolean alarm = false;

        if (maxEnergy > 0) {
            int stored = handler.getEnergyStored(ForgeDirection.DOWN);
            ratio = 1 + (stored * 5) / maxEnergy;
            if (ratio < 1) {
                ratio = 1;
            } else if (ratio > 5) {
                ratio = 5;
            }

            switch (alarmMode) {
                case MODE_OFF:
                    alarm = false;
                    break;
                case MODE_LESS:
                    alarm = ((stored * 100 / maxEnergy) < alarmLevel);
                    break;
                case MODE_MORE:
                    alarm = ((stored * 100 / maxEnergy) > alarmLevel);
                    break;
            }

        }
        if (rflevel.getValue() != ratio || alarm != inAlarm.getValue()) {
            rflevel.setValue(ratio);
            if (inAlarm.getValue() != alarm) {
                inAlarm.setValue(alarm);
            }
            notifyBlockUpdate();
        }
    }


    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        monitorX = tagCompound.getInteger("monitorX");
        monitorY = tagCompound.getInteger("monitorY");
        monitorZ = tagCompound.getInteger("monitorZ");
        rflevel.setValue(tagCompound.getInteger("rflevel"));
        alarmMode = RFMonitorMode.getModeFromIndex(tagCompound.getByte("alarmMode"));
        alarmLevel = tagCompound.getByte("alarmLevel");
        inAlarm.setValue(tagCompound.getBoolean("inAlarm"));
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("monitorX", monitorX);
        tagCompound.setInteger("monitorY", monitorY);
        tagCompound.setInteger("monitorZ", monitorZ);
        tagCompound.setInteger("rflevel", rflevel.getValue());
        tagCompound.setByte("alarmMode", (byte) alarmMode.getIndex());
        tagCompound.setByte("alarmLevel", (byte) alarmLevel);
        tagCompound.setBoolean("inAlarm", inAlarm.getValue());
    }
}
