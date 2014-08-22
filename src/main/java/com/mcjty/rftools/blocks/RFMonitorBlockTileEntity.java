package com.mcjty.rftools.blocks;

import com.mcjty.rftools.Coordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class RFMonitorBlockTileEntity extends TileEntity {
    private int monitorX = -1;
    private int monitorY = -1;  // Invalid y coordinate so we know it is not initialized yet
    private int monitorZ = -1;

    public int getMonitorX() {
        return monitorX;
    }

    public void setMonitorX(int monitorX) {
        this.monitorX = monitorX;
    }

    public int getMonitorY() {
        return monitorY;
    }

    public void setMonitorY(int monitorY) {
        this.monitorY = monitorY;
    }

    public int getMonitorZ() {
        return monitorZ;
    }

    public void setMonitorZ(int monitorZ) {
        this.monitorZ = monitorZ;
    }

    public boolean isValid() {
        return monitorY >= 0;
    }

    public void setInvalid() {
        monitorX = -1;
        monitorY = -1;
        monitorZ = -1;
    }

    public void setMonitor(Coordinate c) {
        System.out.println("setMonitor: "+this);
        monitorX = c.getX();
        monitorY = c.getY();
        monitorZ = c.getZ();
    }
//
//    @Override
//    public Packet getDescriptionPacket() {
//        System.err.println("getDescriptionPacket");
//        NBTTagCompound nbtTag = new NBTTagCompound();
//        this.writeToNBT(nbtTag);
//        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
//    }
//
//    @Override
//    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
//        System.err.println("onDataPacket");
//        readFromNBT(packet.func_148857_g());
//    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        monitorX = tagCompound.getInteger("monitorX");
        monitorY = tagCompound.getInteger("monitorY");
        monitorZ = tagCompound.getInteger("monitorZ");
        System.out.println("readFromNBT: "+ this);
        System.out.println("  monitorX = " + monitorX);
        System.out.println("  monitorY = " + monitorY);
        System.out.println("  monitorZ = " + monitorZ);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("monitorX", monitorX);
        tagCompound.setInteger("monitorY", monitorY);
        tagCompound.setInteger("monitorZ", monitorZ);
        System.out.println("writeToNBT: " + this);
        System.out.println("  monitorX = " + monitorX);
        System.out.println("  monitorY = " + monitorY);
        System.out.println("  monitorZ = " + monitorZ);

    }
}
