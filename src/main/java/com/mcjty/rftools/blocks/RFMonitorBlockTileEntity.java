package com.mcjty.rftools.blocks;

import cofh.api.energy.IEnergyHandler;
import com.mcjty.rftools.Coordinate;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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
    private boolean alarmIfMore;        // If true we give redstone signal if the level goes above a certain value
    private int alarmLevel;             // The level (in percentage) at which we give an alarm

    // Temporary data
    private int counter = 20;
    private int client_rf_level = -1;

    public boolean isAlarmIfMore() {
        return alarmIfMore;
    }

    public int getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarm(boolean mode, int level) {
        alarmIfMore = mode;
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
        if (maxEnergy > 0) {
            int stored = handler.getEnergyStored(ForgeDirection.DOWN);
            ratio = 1 + (stored * 5) / maxEnergy;
            if (ratio < 1) {
                ratio = 1;
            } else if (ratio > 5) {
                ratio = 5;
            }
        }
        if (rflevel != ratio) {
            rflevel = ratio;
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
        System.out.println("RFMonitorBlockTileEntity.onDataPacket");
        readFromNBT(packet.func_148857_g());
    }



    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        monitorX = tagCompound.getInteger("monitorX");
        monitorY = tagCompound.getInteger("monitorY");
        monitorZ = tagCompound.getInteger("monitorZ");
        rflevel = tagCompound.getInteger("rflevel");
        alarmIfMore = tagCompound.getBoolean("alarmMode");
        alarmLevel = tagCompound.getByte("alarmLevel");
        System.out.print("readFromNBT: "+ this);
        System.out.print("  monitorX = " + monitorX);
        System.out.print("  monitorY = " + monitorY);
        System.out.print("  monitorZ = " + monitorZ);
        System.out.println("  rflevel = " + rflevel);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("monitorX", monitorX);
        tagCompound.setInteger("monitorY", monitorY);
        tagCompound.setInteger("monitorZ", monitorZ);
        tagCompound.setInteger("rflevel", rflevel);
        tagCompound.setBoolean("alarmMode", alarmIfMore);
        tagCompound.setByte("alarmLevel", (byte) alarmLevel);
        System.out.print("writeToNBT: " + this);
        System.out.print("  monitorX = " + monitorX);
        System.out.print("  monitorY = " + monitorY);
        System.out.print("  monitorZ = " + monitorZ);
        System.out.println("  rflevel = " + rflevel);

    }
}
