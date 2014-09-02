package com.mcjty.rftools.blocks.monitor;

import cofh.api.energy.IEnergyHandler;
import com.mcjty.rftools.Coordinate;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.blocks.ModBlocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class RFMonitorBlockTileEntity extends TileEntity {
    // Data that is saved
    private int monitorX = -1;
    private int monitorY = -1;  // Invalid y coordinate so we know it is not initialized yet
    private int monitorZ = -1;
    private int rflevel = 0;
    private RFMonitorMode alarmMode = RFMonitorMode.MODE_OFF;
    private int alarmLevel = 0;             // The level (in percentage) at which we give an alarm
    private boolean inAlarm = false;        // If true we are in alarm right now

    // Temporary data
    private int counter = 20;
    private int client_rf_level = -1;
    private int client_inAlarm = -1;

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

    public void setInvalid() {
        monitorX = -1;
        monitorY = -1;
        monitorZ = -1;
        rflevel = 0;
        client_rf_level = -1;
        client_inAlarm = -1;
        inAlarm = false;
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, BlockTools.setRedstoneSignal(meta, inAlarm), 2);
        worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, ModBlocks.monitorBlock);
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public void setMonitor(Coordinate c) {
        monitorX = c.getX();
        monitorY = c.getY();
        monitorZ = c.getZ();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public void updateEntity() {
        if (worldObj.isRemote) {
            checkRFStateClient();
        } else {
            checkRFStateServer();
        }
    }

    public int getRflevel() {
        return rflevel;
    }

    private void checkRFStateClient() {
        if (client_rf_level != rflevel) {
            client_rf_level = rflevel;
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
        if (client_inAlarm != (inAlarm ? 1 : 0)) {
            System.out.println("com.mcjty.rftools.blocks.RFMonitorBlockTileEntity.checkRFStateClient");
            client_inAlarm = (inAlarm ? 1 : 0);
            int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, BlockTools.setRedstoneSignal(meta, inAlarm), 2);
            worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, ModBlocks.monitorBlock);
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    private void checkRFStateServer() {
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
        if (rflevel != ratio || alarm != inAlarm) {
            rflevel = ratio;
            if (inAlarm != alarm) {
                inAlarm = alarm;
                int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
                worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, BlockTools.setRedstoneSignal(meta, inAlarm), 2);
                worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, ModBlocks.monitorBlock);
            }
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        this.writeToNBT(nbtTag);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
    }



    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        monitorX = tagCompound.getInteger("monitorX");
        monitorY = tagCompound.getInteger("monitorY");
        monitorZ = tagCompound.getInteger("monitorZ");
        rflevel = tagCompound.getInteger("rflevel");
        alarmMode = RFMonitorMode.getModeFromIndex(tagCompound.getByte("alarmMode"));
        alarmLevel = tagCompound.getByte("alarmLevel");
        inAlarm = tagCompound.getBoolean("inAlarm");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("monitorX", monitorX);
        tagCompound.setInteger("monitorY", monitorY);
        tagCompound.setInteger("monitorZ", monitorZ);
        tagCompound.setInteger("rflevel", rflevel);
        tagCompound.setByte("alarmMode", (byte) alarmMode.getIndex());
        tagCompound.setByte("alarmLevel", (byte) alarmLevel);
        tagCompound.setBoolean("inAlarm", inAlarm);
    }
}
