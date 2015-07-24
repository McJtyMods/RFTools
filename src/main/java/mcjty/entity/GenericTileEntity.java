package mcjty.entity;

import mcjty.base.GeneralConfig;
import mcjty.network.Argument;
import mcjty.network.ClientCommandHandler;
import mcjty.network.CommandHandler;
import mcjty.varia.Coordinate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GenericTileEntity extends TileEntity implements CommandHandler, ClientCommandHandler {

    private List<SyncedObject> syncedObjects = new ArrayList<SyncedObject>();
    private Coordinate coordinate;
    private int infused = 0;

    private String ownerName = "";
    private UUID ownerUUID = null;
    private int securityChannel = -1;

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

    /// Called by GenericBlock.checkRedstoneWithTE() to set the redstone/powered state of this TE.
    public void setPowered(int powered) {
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
        return ((float) infused) / GeneralConfig.maxInfuse;
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
        ownerName = tagCompound.getString("owner");
        if (tagCompound.hasKey("ownerM")) {
            ownerUUID = new UUID(tagCompound.getLong("ownerM"), tagCompound.getLong("ownerL"));
        } else {
            ownerUUID = null;
        }
        if (tagCompound.hasKey("secChannel")) {
            securityChannel = tagCompound.getInteger("secChannel");
        } else {
            securityChannel = -1;
        }
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
        if (ownerUUID != null) {
            tagCompound.setString("owner", ownerName);
            tagCompound.setLong("ownerM", ownerUUID.getMostSignificantBits());
            tagCompound.setLong("ownerL", ownerUUID.getLeastSignificantBits());
        }
        if (securityChannel != -1) {
            tagCompound.setInteger("secChannel", securityChannel);
        }
    }

    public boolean setOwner(EntityPlayer player) {
        if (ownerUUID != null) {
            // Already has an owner.
            return false;
        }
        ownerUUID = player.getPersistentID();
        ownerName = player.getDisplayName();
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);

        return true;
    }

    public void clearOwner() {
        ownerUUID = null;
        ownerName = "";
        securityChannel = -1;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public void setSecurityChannel(int id) {
        securityChannel = id;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public int getSecurityChannel() {
        return securityChannel;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
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
