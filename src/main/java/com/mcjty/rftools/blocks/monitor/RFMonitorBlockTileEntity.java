package com.mcjty.rftools.blocks.monitor;

import cofh.api.energy.IEnergyHandler;
import com.mcjty.entity.GenericTileEntity;
import com.mcjty.entity.SyncedValue;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.network.Argument;
import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RFMonitorBlockTileEntity extends GenericTileEntity {
    // Data that is saved
    private int monitorX = -1;
    private int monitorY = -1;  // Invalid y coordinate so we know it is not initialized yet
    private int monitorZ = -1;
    private RFMonitorMode alarmMode = RFMonitorMode.MODE_OFF;
    private int alarmLevel = 0;             // The level (in percentage) at which we give an alarm

    public static final String CMD_GETADJACENTBLOCKS = "getAdj";
    public static final String CLIENTCMD_ADJACENTBLOCKSREADY = "adjReady";

    // Temporary data
    private int counter = 20;

    private SyncedValue<Integer> rflevel = new SyncedValue<Integer>(0);
    private SyncedValue<Boolean> inAlarm = new SyncedValue<Boolean>(false);

    // Client side only
    private List<Coordinate> clientAdjacentBlocks = null;

    public RFMonitorBlockTileEntity() {
        registerSyncedObject(rflevel);
        registerSyncedObject(inAlarm);
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
        Boolean value = inAlarm.getValue();
        return BlockTools.setRedstoneSignal(meta, value == null ? false : value);
    }

    public List<Coordinate> findAdjacentBlocks() {
        int x = xCoord;
        int y = yCoord;
        int z = zCoord;
        List<Coordinate> adjacentBlocks = new ArrayList<Coordinate>();
        for (int dy = -1 ; dy <= 1 ; dy++) {
            int yy = y + dy;
            if (yy >= 0 && yy < worldObj.getActualHeight()) {
                for (int dz = -1 ; dz <= 1 ; dz++) {
                    int zz = z + dz;
                    for (int dx = -1 ; dx <= 1 ; dx++) {
                        int xx = x + dx;
                        if (dx != 0 || dy != 0 || dz != 0) {
                            TileEntity tileEntity = worldObj.getTileEntity(xx, yy, zz);
                            if (tileEntity != null) {
                                if (tileEntity instanceof IEnergyHandler) {
                                    adjacentBlocks.add(new Coordinate(xx, yy, zz));
                                }
                            }
                        }
                    }
                }
            }
        }
        return adjacentBlocks;
    }

    public void storeAdjacentBlocksForClient(List<Coordinate> coordinates) {
        clientAdjacentBlocks = new ArrayList<Coordinate>(coordinates);
    }

    public List<Coordinate> getClientAdjacentBlocks() {
        return clientAdjacentBlocks;
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
        Boolean v = inAlarm.getValue();
        boolean alarmValue = v == null ? false : v;
        if (rflevel.getValue() != ratio || alarm != alarmValue) {
            rflevel.setValue(ratio);
            if (alarmValue != alarm) {
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
        Boolean value = inAlarm.getValue();
        tagCompound.setBoolean("inAlarm", value == null ? false : value);
    }

    @Override
    public List executeWithResult(String command, Map<String, Argument> args) {
        List rc = super.executeWithResult(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETADJACENTBLOCKS.equals(command)) {
            return findAdjacentBlocks();
        }
        return null;
    }

    @Override
    public boolean execute(String command, List list) {
        boolean rc = super.execute(command, list);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_ADJACENTBLOCKSREADY.equals(command)) {
            storeAdjacentBlocksForClient((List<Coordinate>) list);
            return true;
        }
        return false;
    }
}
