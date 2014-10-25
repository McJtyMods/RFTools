package com.mcjty.entity;

import com.mcjty.rftools.network.Argument;
import com.mcjty.rftools.network.ClientCommandHandler;
import com.mcjty.rftools.network.CommandHandler;
import com.mcjty.varia.Coordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenericTileEntity extends TileEntity implements CommandHandler, ClientCommandHandler {

    private List<SyncedObject> syncedObjects = new ArrayList<SyncedObject>();
    private Coordinate coordinate;

    public void setInvalid() {
        for (SyncedObject value : syncedObjects) {
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
        for (SyncedObject value : syncedObjects) {
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

    protected void registerSyncedObject(SyncedObject value) {
        syncedObjects.add(value);
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
    public boolean execute(String command, Map<String, Argument> args) {
        return false;
    }

    @Override
    public List executeWithResultList(String command, Map<String, Argument> args) {
        return null;
    }

    @Override
    public Integer executeWithResultInteger(String command, Map<String, Argument> args) {
        return null;
    }

    @Override
    public boolean execute(String command, List list) {
        return false;
    }

    @Override
    public boolean execute(String command, Integer result) {
        return false;
    }

    public Coordinate getCoordinate() {
        if (coordinate == null) {
            coordinate = new Coordinate(xCoord, yCoord, zCoord);
        }
        return coordinate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GenericTileEntity)) return false;
        return getCoordinate().equals(((GenericTileEntity) o).getCoordinate());
    }

    @Override
    public int hashCode() {
        return getCoordinate().hashCode();
    }
}
