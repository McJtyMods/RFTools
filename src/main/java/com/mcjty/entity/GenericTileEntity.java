package com.mcjty.entity;

import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.network.Argument;
import com.mcjty.rftools.network.ClientCommandHandler;
import com.mcjty.rftools.network.CommandHandler;
import com.mcjty.varia.Coordinate;
import net.minecraft.item.ItemStack;
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
    private int infused = 0;

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

    // Called when a slot is changed.
    public void onSlotChanged(int index, ItemStack stack) {
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

    public void setInfused(int infused) {
        this.infused = infused;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public int getInfused() {
        return infused;
    }

    public float getInfusedFactor() {
        return ((float) infused) / DimletConfiguration.maxInfuse;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        readRestorableFromNBT(tagCompound);
    }

    /**
     * Override this method to recover all information that you want
     * to recover from an ItemBlock in the player's inventory.
     * @param tagCompound
     */
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        infused = tagCompound.getInteger("infused");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        writeRestorableToNBT(tagCompound);
    }

    /**
     * Override this method to store all information that you want
     * to store in an ItemBlock in the player's inventory (when the block
     * is picked up with a wrench).
     * @param tagCompound
     */
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        tagCompound.setInteger("infused", infused);
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
}
