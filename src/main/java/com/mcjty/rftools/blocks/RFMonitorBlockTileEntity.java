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
        worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 2);
    }

    public void setMonitor(Coordinate c) {
        monitorX = c.getX();
        monitorY = c.getY();
        monitorZ = c.getZ();
    }

    @Override
    public void updateEntity() {
        counter--;
        if (counter <= 0) {
            counter = 20;
            checkRFState();
        }
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
                worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, ratio, 2);
            }
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
