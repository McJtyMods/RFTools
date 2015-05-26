package mcjty.rftools.blocks.storage;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import mcjty.container.InventoryHelper;
import mcjty.entity.GenericTileEntity;
import mcjty.rftools.ClientInfo;
import mcjty.rftools.items.storage.StorageModuleItem;
import mcjty.rftools.network.Argument;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;

import java.util.Map;

public class ModularStorageTileEntity extends GenericTileEntity implements ISidedInventory {

    public static final String CMD_SETTINGS = "settings";
    public static final String CMD_COMPACT = "compact";
    public static final String CMD_CYCLE = "cycle";

    private int[] accessible = null;
    private int maxSize = 0;

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ModularStorageContainer.factory, 2 + ModularStorageContainer.MAXSIZE_STORAGE);

    private String sortMode = "";
    private String viewMode = "";
    private boolean groupMode = false;
    private String filter = "";

    private int numStacks = -1;       // -1 means no storage cell.
    private int remoteId = 0;

    private int prevLevel = -3;     // -3 means to check, -2 means invalid
    private int timer = 10;

    @Override
    protected void checkStateServer() {
        timer--;
        if (timer > 0) {
            return;
        }
        timer = 10;

        if (isRemote()) {
            // Only if we have a remote storage module do we have to do anything.
            RemoteStorageTileEntity storageTileEntity = getRemoteStorage(remoteId);
            int si = -1;
            if (storageTileEntity != null) {
                si = storageTileEntity.findRemoteIndex(remoteId);
            }
            if (si == -1) {
                if (prevLevel != -2) {
                    prevLevel = -2;
                    numStacks = -1;
                    maxSize = 0;
                    markDirty();
                    worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                }
                return;
            }

            numStacks = storageTileEntity.getCount(si);
            maxSize = storageTileEntity.getMaxStacks(si);
            int level = getRenderLevel();
            if (level != prevLevel) {
                prevLevel = level;
                markDirty();
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }
        }
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        if (accessible == null) {
            accessible = new int[maxSize];
            for (int i = 0 ; i < maxSize ; i++) {
                accessible[i] = 2 + i;
            }
        }
        return accessible;
    }

    public boolean isGroupMode() {
        return groupMode;
    }

    public void setGroupMode(boolean groupMode) {
        this.groupMode = groupMode;
        markDirty();
    }

    public String getSortMode() {
        return sortMode;
    }

    public void setSortMode(String sortMode) {
        this.sortMode = sortMode;
        markDirty();
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
        markDirty();
    }

    public String getViewMode() {
        return viewMode;
    }

    public void setViewMode(String viewMode) {
        this.viewMode = viewMode;
        markDirty();
    }

    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack item, int side) {
        return index >= ModularStorageContainer.SLOT_STORAGE && isItemValidForSlot(index, item);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack item, int side) {
        return index >= ModularStorageContainer.SLOT_STORAGE && isItemValidForSlot(index, item);
    }

    @Override
    public int getSizeInventory() {
        return ModularStorageContainer.SLOT_STORAGE + maxSize;
    }

    private boolean containsItem(int index) {
        if (isStorageAvailableRemotely(index)) {
            index -= ModularStorageContainer.SLOT_STORAGE;
            RemoteStorageTileEntity storageTileEntity = getRemoteStorage(remoteId);
            if (storageTileEntity == null) {
                return false;
            }
            ItemStack[] slots = storageTileEntity.findStacksForId(remoteId);
            if (slots == null || index >= slots.length) {
                return false;
            }
            return slots[index] != null;
        } else {
            return inventoryHelper.containsItem(index);
        }
    }

    // On server, and if we have a remote storage module and if we're accessing a remote slot we check the remote storage.
    private boolean isStorageAvailableRemotely(int index) {
        return isServer() && isRemote() && index >= ModularStorageContainer.SLOT_STORAGE;
    }

    private boolean isRemote() {
        return remoteId != 0;
    }

    public int getRemoteId() {
        return remoteId;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if (index >= getSizeInventory()) {
            return null;
        }
        if (isStorageAvailableRemotely(index)) {
            index -= ModularStorageContainer.SLOT_STORAGE;
            RemoteStorageTileEntity storageTileEntity = getRemoteStorage(remoteId);
            if (storageTileEntity == null) {
                return null;
            }
            ItemStack[] slots = storageTileEntity.findStacksForId(remoteId);
            if (slots == null || index >= slots.length) {
                return null;
            }
            return slots[index];
        }
        return inventoryHelper.getStackInSlot(index);
    }

    private void handleNewAmount(boolean s1, int index) {
        if (index < ModularStorageContainer.SLOT_STORAGE) {
            return;
        }
        boolean s2 = containsItem(index);
        if (s1 == s2) {
            return;
        }

        int rlold = getRenderLevel();

        if (s1) {
            numStacks--;
        } else {
            numStacks++;
        }
        StorageModuleItem.updateStackSize(getStackInSlot(ModularStorageContainer.SLOT_STORAGE_MODULE), numStacks);

        int rlnew = getRenderLevel();
        if (rlold != rlnew) {
            markDirty();
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    public int getRenderLevel() {
        if (numStacks == -1 || maxSize == 0) {
            return -1;
        }
        return (numStacks+6) * 7 / maxSize;
    }

    public int getNumStacks() {
        return numStacks;
    }

    private ItemStack decrStackSizeHelper(int index, int amount) {
        if (isStorageAvailableRemotely(index)) {
            index -= ModularStorageContainer.SLOT_STORAGE;
            RemoteStorageTileEntity storageTileEntity = getRemoteStorage(remoteId);
            if (storageTileEntity == null) {
                return null;
            }

            int si = storageTileEntity.findRemoteIndex(remoteId);
            if (si == -1) {
                return null;
            }
            return storageTileEntity.decrStackSizeRemote(si, index, amount);
        } else {
            return inventoryHelper.decrStackSize(index, amount);
        }
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        boolean s1 = containsItem(index);
        ItemStack itemStack = decrStackSizeHelper(index, amount);
        handleNewAmount(s1, index);

        if (index == ModularStorageContainer.SLOT_STORAGE_MODULE) {
            copyFromModule(inventoryHelper.getStackInSlot(ModularStorageContainer.SLOT_STORAGE_MODULE));
        }
        return itemStack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    private void setInventorySlotContentsHelper(int limit, int index, ItemStack stack) {
        if (isStorageAvailableRemotely(index)) {
            index -= ModularStorageContainer.SLOT_STORAGE;
            RemoteStorageTileEntity storageTileEntity = getRemoteStorage(remoteId);
            if (storageTileEntity == null) {
                return;
            }

            int si = storageTileEntity.findRemoteIndex(remoteId);
            if (si == -1) {
                return;
            }
            storageTileEntity.updateRemoteSlot(si, limit, index, stack);
        } else {
            inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
        }
    }

    public void syncToClient() {
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index == ModularStorageContainer.SLOT_STORAGE_MODULE) {
            if (isServer()) {
                copyFromModule(stack);
            }
        } else if (index == ModularStorageContainer.SLOT_TYPE_MODULE) {
            // Make sure front side is updated.
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
        boolean s1 = containsItem(index);
        setInventorySlotContentsHelper(getInventoryStackLimit(), index, stack);
        handleNewAmount(s1, index);
    }

    @Override
    public String getInventoryName() {
        return "Modular Storage Inventory";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index >= getSizeInventory()) {
            return false;
        }

        if (isStorageAvailableRemotely(index)) {
            index -= ModularStorageContainer.SLOT_STORAGE;
            RemoteStorageTileEntity storageTileEntity = getRemoteStorage(remoteId);
            if (storageTileEntity == null) {
                return false;
            }

            ItemStack[] stacks = storageTileEntity.findStacksForId(remoteId);
            if (stacks == null || index >= stacks.length) {
                return false;
            }
        }
        return true;
    }

    public void copyToModule() {
        ItemStack stack = inventoryHelper.getStackInSlot(ModularStorageContainer.SLOT_STORAGE_MODULE);
        if (stack == null) {
            // Should be impossible.
            return;
        }
        if (stack.getItemDamage() == StorageModuleItem.STORAGE_REMOTE) {
            return;
        }
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            stack.setTagCompound(tagCompound);
        }
        int cnt = writeBufferToNBT(tagCompound, ModularStorageContainer.SLOT_STORAGE);
        tagCompound.setInteger("count", cnt);

        for (int i = ModularStorageContainer.SLOT_STORAGE ; i < inventoryHelper.getCount() ; i++) {
            inventoryHelper.setInventorySlotContents(0, i, null);
        }
        numStacks = -1;
        remoteId = 0;

        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public void copyFromModule(ItemStack stack) {
        for (int i = ModularStorageContainer.SLOT_STORAGE ; i < inventoryHelper.getCount() ; i++) {
            inventoryHelper.setInventorySlotContents(0, i, null);
        }

        if (stack == null) {
            setMaxSize(0);
            numStacks = -1;
            return;
        }
        remoteId = 0;
        if (stack.getItemDamage() == StorageModuleItem.STORAGE_REMOTE) {
            NBTTagCompound tagCompound = stack.getTagCompound();
            if (tagCompound == null || !tagCompound.hasKey("id")) {
                setMaxSize(0);
                numStacks = -1;
                return;
            }
            remoteId = tagCompound.getInteger("id");
            RemoteStorageTileEntity remoteStorageTileEntity = getRemoteStorage(remoteId);
            if (remoteStorageTileEntity == null) {
                setMaxSize(0);
                numStacks = -1;
                return;
            }
            ItemStack storageStack = remoteStorageTileEntity.findStorageWithId(remoteId);
            if (storageStack == null) {
                setMaxSize(0);
                numStacks = -1;
                return;
            }
            setMaxSize(StorageModuleItem.MAXSIZE[storageStack.getItemDamage()]);
        } else {
            NBTTagCompound tagCompound = stack.getTagCompound();
            if (tagCompound != null) {
                readBufferFromNBT(tagCompound, ModularStorageContainer.SLOT_STORAGE);
            }
            setMaxSize(StorageModuleItem.MAXSIZE[stack.getItemDamage()]);
        }

        updateStackCount();
    }

    private RemoteStorageTileEntity getRemoteStorage(int id) {
        World world = getWorld();
        return RemoteStorageIdRegistry.getRemoteStorage(world, id);

    }

    private void updateStackCount() {
        numStacks = 0;
        if (isServer() && isRemote()) {
            RemoteStorageTileEntity storageTileEntity = getRemoteStorage(remoteId);
            if (storageTileEntity == null) {
                return;
            }
            int si = storageTileEntity.findRemoteIndex(remoteId);
            if (si == -1) {
                return;
            }
            ItemStack[] stacks = storageTileEntity.getRemoteStacks(si);
            for (int i = 0; i < maxSize; i++) {
                if (stacks[i] != null) {
                    numStacks++;
                }
            }
            storageTileEntity.updateCount(si, numStacks);
        } else {
            for (int i = 2; i < 2 + maxSize; i++) {
                if (inventoryHelper.containsItem(i)) {
                    numStacks++;
                }
            }
        }
        StorageModuleItem.updateStackSize(getStackInSlot(ModularStorageContainer.SLOT_STORAGE_MODULE), numStacks);
    }

    private boolean isServer() {
        if (worldObj != null) {
            return !worldObj.isRemote;
        } else {
            return FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER;
        }
    }

    private World getWorld() {
        World world = worldObj;
        if (world == null) {
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
                world = ClientInfo.getWorld();
            } else {
                world = DimensionManager.getWorld(0);
            }
        }
        return world;
    }

    private void setMaxSize(int ms) {
        maxSize = ms;
        inventoryHelper.setNewCount(ModularStorageContainer.SLOT_STORAGE + maxSize);
        accessible = null;

        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        numStacks = tagCompound.getInteger("numStacks");
        maxSize = tagCompound.getInteger("maxSize");
//        System.out.println((isServer() ? "SERVER" : "CLIENT") + ": loc=" + xCoord + "," + yCoord + "," + zCoord + ", #stacks=" + numStacks + ", max=" + maxSize);
        remoteId = tagCompound.getInteger("remoteId");
        sortMode = tagCompound.getString("sortMode");
        viewMode = tagCompound.getString("viewMode");
        groupMode = tagCompound.getBoolean("groupMode");
        filter = tagCompound.getString("filter");
        readBufferFromNBT(tagCompound, 0);
        inventoryHelper.setNewCount(ModularStorageContainer.SLOT_STORAGE + maxSize);
        accessible = null;

        if (isServer()) {
            updateStackCount();
        }
    }

    private void readBufferFromNBT(NBTTagCompound tagCompound, int offset) {
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            inventoryHelper.setStackInSlot(i+offset, ItemStack.loadItemStackFromNBT(nbtTagCompound));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, 0);
        tagCompound.setInteger("numStacks", numStacks);
        tagCompound.setInteger("maxSize", maxSize);
        tagCompound.setInteger("remoteId", remoteId);
        tagCompound.setString("sortMode", sortMode);
        tagCompound.setString("viewMode", viewMode);
        tagCompound.setBoolean("groupMode", groupMode);
        tagCompound.setString("filter", filter);
    }

    private int writeBufferToNBT(NBTTagCompound tagCompound, int offset) {
        // If sendToClient is true we have to send dummy information to the client
        // so that it can remotely open gui's.
        boolean sendToClient = isServer() && offset == 0 && isRemote();

        int cnt = 0;
        NBTTagList bufferTagList = new NBTTagList();
        if (sendToClient) {
            for (int i = 0 ; i < ModularStorageContainer.SLOT_STORAGE ; i++) {
                ItemStack stack = inventoryHelper.getStackInSlot(i);
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                if (stack != null) {
                    stack.writeToNBT(nbtTagCompound);
                }
                bufferTagList.appendTag(nbtTagCompound);
            }
            RemoteStorageTileEntity storageTileEntity = getRemoteStorage(remoteId);
            if (storageTileEntity != null) {
                ItemStack[] slots = storageTileEntity.findStacksForId(remoteId);
                if (slots != null) {
                    for (ItemStack stack : slots) {
                        NBTTagCompound nbtTagCompound = new NBTTagCompound();
                        if (stack != null) {
                            stack.writeToNBT(nbtTagCompound);
                            if (stack.stackSize > 0) {
                                cnt++;
                            }
                        }
                        bufferTagList.appendTag(nbtTagCompound);
                    }
                }
            }
        } else {
            for (int i = offset; i < inventoryHelper.getCount(); i++) {
                ItemStack stack = inventoryHelper.getStackInSlot(i);
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                if (stack != null) {
                    stack.writeToNBT(nbtTagCompound);
                    if (stack.stackSize > 0) {
                        cnt++;
                    }
                }
                bufferTagList.appendTag(nbtTagCompound);
            }
        }
        tagCompound.setTag("Items", bufferTagList);
        return cnt;
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETTINGS.equals(command)) {
            setFilter(args.get("filter").getString());
            setViewMode(args.get("viewMode").getString());
            setSortMode(args.get("sortMode").getString());
            setGroupMode(args.get("groupMode").getBoolean());
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            return true;
        } else if (CMD_COMPACT.equals(command)) {
            compact();
            return true;
        } else if (CMD_CYCLE.equals(command)) {
            cycle();
            return true;
        }
        return false;
    }

    private void cycle() {
        if (isRemote()) {
            RemoteStorageTileEntity storageTileEntity = getRemoteStorage(remoteId);
            if (storageTileEntity == null) {
                return;
            }
            remoteId = storageTileEntity.cycle(remoteId);
            getStackInSlot(ModularStorageContainer.SLOT_STORAGE_MODULE).getTagCompound().setInteger("id", remoteId);
            markDirty();
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    private void compact() {
        if (isRemote()) {
            RemoteStorageTileEntity storageTileEntity = getRemoteStorage(remoteId);
            if (storageTileEntity == null) {
                return;
            }
            storageTileEntity.compact(remoteId);
        } else {
            InventoryHelper.compactStacks(inventoryHelper, ModularStorageContainer.SLOT_STORAGE, maxSize);
        }

        updateStackCount();
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }


}
