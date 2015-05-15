package mcjty.rftools.blocks.storage;

import mcjty.container.InventoryHelper;
import mcjty.entity.GenericTileEntity;
import mcjty.rftools.items.storage.StorageModuleItem;
import mcjty.rftools.network.Argument;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.Map;

public class ModularStorageTileEntity extends GenericTileEntity implements ISidedInventory {

    public static final String CMD_SETTINGS = "settings";

    private int[] accessible = null;
    private int maxSize = 0;

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ModularStorageContainer.factory, 2 + ModularStorageContainer.MAXSIZE_STORAGE);

    private String sortMode = "";
    private String viewMode = "";
    private boolean groupMode = false;
    private String filter = "";

    private int numStacks = -1;       // -1 means no storage cell.

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
        return index >= 2;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack item, int side) {
        return index >= 2;
    }

    @Override
    public int getSizeInventory() {
        return 2 + maxSize;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if (index >= getSizeInventory()) {
            return null;
        }
        return inventoryHelper.getStacks()[index];
    }

    private void handleNewAmount(boolean s1, int index) {
        if (index < ModularStorageContainer.SLOT_STORAGE) {
            return;
        }
        boolean s2 = inventoryHelper.containsItem(index);
        if (s1 == s2) {
            return;
        }

        int rlold = getRenderLevel();

        if (s1) {
            numStacks--;
        } else {
            numStacks++;
        }
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

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        boolean s1 = inventoryHelper.containsItem(index);
        ItemStack itemStack = inventoryHelper.decrStackSize(index, amount);
        handleNewAmount(s1, index);

        if (index == ModularStorageContainer.SLOT_STORAGE_MODULE) {
            copyFromModule(inventoryHelper.getStacks()[ModularStorageContainer.SLOT_STORAGE_MODULE]);
        }
        return itemStack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index == ModularStorageContainer.SLOT_STORAGE_MODULE) {
            copyFromModule(stack);
        } else if (index == ModularStorageContainer.SLOT_TYPE_MODULE) {
            // Make sure front side is updated.
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
        boolean s1 = inventoryHelper.containsItem(index);
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
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
        return true;
    }

    public void copyToModule() {
        ItemStack stack = inventoryHelper.getStacks()[ModularStorageContainer.SLOT_STORAGE_MODULE];
        if (stack == null || stack.stackSize == 0) {
            // Should be impossible.
            return;
        }
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            stack.setTagCompound(tagCompound);
        }
        writeBufferToNBT(tagCompound, ModularStorageContainer.SLOT_STORAGE);

        for (int i = ModularStorageContainer.SLOT_STORAGE ; i < inventoryHelper.getStacks().length ; i++) {
            inventoryHelper.setInventorySlotContents(0, i, null);
        }
        numStacks = -1;

        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public void copyFromModule(ItemStack stack) {
        for (int i = ModularStorageContainer.SLOT_STORAGE ; i < inventoryHelper.getStacks().length ; i++) {
            inventoryHelper.setInventorySlotContents(0, i, null);
        }

        if (stack == null || stack.stackSize == 0) {
            setMaxSize(0);
            numStacks = -1;
            return;
        }

        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null) {
            readBufferFromNBT(tagCompound, ModularStorageContainer.SLOT_STORAGE);
        }

        setMaxSize(StorageModuleItem.MAXSIZE[stack.getItemDamage()]);
        updateStackCount();
    }

    private void updateStackCount() {
        numStacks = 0;
        for (int i = 2 ; i < 2+maxSize ; i++) {
            if (inventoryHelper.containsItem(i)) {
                numStacks++;
            }
        }
    }

    private void setMaxSize(int ms) {
        maxSize = ms;
        inventoryHelper.setNewCount(2 + maxSize);
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
        readBufferFromNBT(tagCompound, 0);
        numStacks = tagCompound.getInteger("numStacks");
        maxSize = tagCompound.getInteger("maxSize");
        sortMode = tagCompound.getString("sortMode");
        viewMode = tagCompound.getString("viewMode");
        groupMode = tagCompound.getBoolean("groupMode");
        filter = tagCompound.getString("filter");
        inventoryHelper.setNewCount(2 + maxSize);
        accessible = null;

        updateStackCount();
    }

    private void readBufferFromNBT(NBTTagCompound tagCompound, int offset) {
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            inventoryHelper.getStacks()[i+offset] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
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
        tagCompound.setString("sortMode", sortMode);
        tagCompound.setString("viewMode", viewMode);
        tagCompound.setBoolean("groupMode", groupMode);
        tagCompound.setString("filter", filter);
    }

    private void writeBufferToNBT(NBTTagCompound tagCompound, int offset) {
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = offset ; i < inventoryHelper.getCount() ; i++) {
            ItemStack stack = inventoryHelper.getStacks()[i];
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
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
        }
        return false;
    }


}
