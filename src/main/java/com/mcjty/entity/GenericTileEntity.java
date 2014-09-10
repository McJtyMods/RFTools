package com.mcjty.entity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

public class GenericTileEntity extends TileEntity {

    private List<SyncedValue> syncedValues = new ArrayList<SyncedValue>();

    public void setInvalid() {
        for (SyncedValue value : syncedValues) {
            value.setInvalid();
        }

        int oldMeta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        int newMeta = updateMetaData(oldMeta);
        if (oldMeta != newMeta) {
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, newMeta, 2);
        }
        worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public void updateEntity() {
        if (worldObj.isRemote) {
            checkStateClient();
        } else {
            checkStateServer();
        }
    }

    protected void checkStateClient() {
        // Sync all values from the server.
        boolean syncNeeded = false;
        for (SyncedValue value : syncedValues) {
            if (!value.isClientValueUptodate()) {
                value.updateClientValue();
                syncNeeded = true;
            }
        }
        if (syncNeeded) {
            notifyBlockUpdate();
        }
    }

    protected void checkStateServer() {
    }

    protected void notifyBlockUpdate() {
        int oldMeta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        int newMeta = updateMetaData(oldMeta);
        if (oldMeta != newMeta) {
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, newMeta, 2);
        }
        worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    protected int updateMetaData(int meta) {
        return meta;
    }

    protected void registerSyncedValue(SyncedValue value) {
        syncedValues.add(value);
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


}
