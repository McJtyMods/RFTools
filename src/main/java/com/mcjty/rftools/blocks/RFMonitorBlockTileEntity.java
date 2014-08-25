package com.mcjty.rftools.blocks;

import cofh.api.energy.IEnergyHandler;
import com.mcjty.rftools.Coordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class RFMonitorBlockTileEntity extends TileEntity {
    private int monitorX = -1;
    private int monitorY = -1;  // Invalid y coordinate so we know it is not initialized yet
    private int monitorZ = -1;

    private int counter = 20;
    private int rflevel = 0;
    private int client_rf_level = -1;

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
    }

    @Override
    public void updateEntity() {
        // @@@ Client side should update when rflevel changes!
        counter--;
        if (counter <= 0) {
            counter = 20;
            checkRFState();
        }
    }

    public int getRflevel() {
        return rflevel;
    }

    private void checkRFState() {
        if (isValid()) {
            if (!worldObj.isRemote) {
                TileEntity tileEntity = worldObj.getTileEntity(monitorX, monitorY, monitorZ);
                if (tileEntity == null || !(tileEntity instanceof IEnergyHandler)) {
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
                rflevel = ratio;
                System.out.println("ratio = " + ratio);
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            } else {
                if (client_rf_level != rflevel) {
                    System.out.println("Client: RFMonitorBlockTileEntity.checkRFState");
                    client_rf_level = rflevel;
                    worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                }
            }
        }
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public Packet getDescriptionPacket() {
        System.out.println("RFMonitorBlockTileEntity.getDescriptionPacket");
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
        System.out.print("writeToNBT: " + this);
        System.out.print("  monitorX = " + monitorX);
        System.out.print("  monitorY = " + monitorY);
        System.out.print("  monitorZ = " + monitorZ);
        System.out.println("  rflevel = " + rflevel);

    }
}
