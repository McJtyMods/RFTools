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
    private int maxSize;

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ModularStorageContainer.factory, 2 + ModularStorageContainer.MAXSIZE_STORAGE);

    private String sortMode;
    private String viewMode;
    private boolean groupMode;
    private String filter;

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
        return inventoryHelper.getStacks()[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        return inventoryHelper.decrStackSize(index, amount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index == ModularStorageContainer.SLOT_STORAGE_MODULE && stack != null) {
            copyFromModule(stack);
        } else if (index == ModularStorageContainer.SLOT_TYPE_MODULE) {
            // Make sure front side is updated.
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
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

        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public void copyFromModule(ItemStack stack) {
        if (stack == null || stack.stackSize == 0) {
            return;
        }

        for (int i = ModularStorageContainer.SLOT_STORAGE ; i < inventoryHelper.getStacks().length ; i++) {
            inventoryHelper.setInventorySlotContents(0, i, null);
        }

        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null) {
            readBufferFromNBT(tagCompound, ModularStorageContainer.SLOT_STORAGE);
        }

        maxSize = StorageModuleItem.MAXSIZE[stack.getItemDamage()];

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
        maxSize = tagCompound.getInteger("maxSize");
        sortMode = tagCompound.getString("sortMode");
        viewMode = tagCompound.getString("viewMode");
        groupMode = tagCompound.getBoolean("groupMode");
        filter = tagCompound.getString("filter");
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
        tagCompound.setInteger("maxSize", maxSize);
        tagCompound.setString("sortMode", sortMode);
        tagCompound.setString("viewMode", viewMode);
        tagCompound.setBoolean("groupMode", groupMode);
        tagCompound.setString("filter", filter);
    }

    private void writeBufferToNBT(NBTTagCompound tagCompound, int offset) {
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = offset ; i < inventoryHelper.getStacks().length ; i++) {
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
