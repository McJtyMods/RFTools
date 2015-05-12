package mcjty.rftools.blocks.storage;

import mcjty.container.InventoryHelper;
import mcjty.entity.GenericTileEntity;
import mcjty.rftools.network.Argument;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModularStorageTileEntity extends GenericTileEntity implements ISidedInventory {

    private int[] accessible = null;

    public static final String CMD_SHIFTCLICK_SLOT = "clickSlotShift";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ModularStorageContainer.factory, 2 + ModularStorageContainer.MAXSIZE_STORAGE);

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        if (accessible == null) {
            accessible = new int[ModularStorageContainer.MAXSIZE_STORAGE];
            for (int i = 0 ; i < ModularStorageContainer.MAXSIZE_STORAGE ; i++) {
                accessible[i] = 2 + i;
            }
        }
        return accessible;
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
        return inventoryHelper.getStacks().length;
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

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound);
    }

    private void readBufferFromNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            inventoryHelper.getStacks()[i] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound);
    }

    private void writeBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = 0 ; i < inventoryHelper.getStacks().length ; i++) {
            ItemStack stack = inventoryHelper.getStacks()[i];
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
    }

    private void dragItem(EntityPlayerMP playerMP, int slot) {
        ItemStack stack = getStackInSlot(slot);
        if (stack == null) {
            return;
        }
        stack = stack.copy();
        inventoryHelper.decrStackSize(slot, stack.stackSize);
        playerMP.inventory.setItemStack(stack);
    }

    private void shiftClickSlot(EntityPlayerMP playerMP, int slot) {
        System.out.println("slot = " + slot);
        ItemStack storageModule = inventoryHelper.getStacks()[ModularStorageContainer.SLOT_STORAGE_MODULE];
        if (storageModule == null) {
            return;
        }

//        ItemStack stack = inventoryHelper.getStacks()[slot];
        ItemStack stack = playerMP.inventory.getStackInSlot(slot);
        if (stack == null) {
            return;
        }
        System.out.println("stack = " + stack);

        List<InventoryHelper.SlotModifier> undo = new ArrayList<InventoryHelper.SlotModifier>();
        int i = inventoryHelper.mergeItemStack(this, stack, 2, 2 + ModularStorageContainer.MAXSIZE_STORAGE, undo);
        playerMP.inventory.decrStackSize(slot, stack.stackSize-i);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_SHIFTCLICK_SLOT.equals(command)) {
            shiftClickSlot(playerMP, args.get("slot").getInteger());
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            return true;
        }
        return false;
    }

}
