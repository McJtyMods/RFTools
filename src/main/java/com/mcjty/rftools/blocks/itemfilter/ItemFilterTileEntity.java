package com.mcjty.rftools.blocks.itemfilter;

import com.mcjty.container.InventoryHelper;
import com.mcjty.entity.GenericTileEntity;
import com.mcjty.rftools.network.Argument;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.Map;

public class ItemFilterTileEntity extends GenericTileEntity implements ISidedInventory {
    public static final String CMD_SETMODE = "setMode";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ItemFilterContainer.factory, ItemFilterContainer.GHOST_SIZE + ItemFilterContainer.BUFFER_SIZE);

    public static final byte MODE_INPUT_EXACT = 2;
    public static final byte MODE_INPUT = 1;
    public static final byte MODE_DISABLED = 0;
    public static final byte MODE_OUTPUT_EXACT = -1;
    public static final byte MODE_OUTPUT = -2;
    private byte inputMode[] = new byte[6];

    public byte[] getInputMode() {
        return inputMode;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound);
        inputMode = tagCompound.getByteArray("inputMode");
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
        tagCompound.setByteArray("inputMode", inputMode);
    }

    private void writeBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (ItemStack stack : inventoryHelper.getStacks()) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETMODE.equals(command)) {
            Integer index = args.get("index").getInteger();
            Integer input = args.get("input").getInteger();
            inputMode[index] = (byte) (int) input;
            markDirty();
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            return true;
        }
        return false;
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
        return "Item Filter Inventory";
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
        if (index < ItemFilterContainer.SLOT_BUFFER) {
            return true;
        }
        ItemStack ghostStack = inventoryHelper.getStacks()[index - ItemFilterContainer.SLOT_BUFFER];
        if (ghostStack == null) {
            return true;
        }
        return ghostStack.isItemEqual(stack);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        int v = ItemFilterContainer.SLOT_BUFFER;
        return new int[] { v, v+1, v+2, v+3, v+4, v+5 };
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, int side) {
        if (index < ItemFilterContainer.SLOT_BUFFER) {
            return false;
        }
        if (!isInputMode(side)) {
            return false;
        }

        int ghostIndex = index - ItemFilterContainer.SLOT_BUFFER;

        if (inputMode[side] == MODE_INPUT_EXACT && ghostIndex != side) {
            return false;       // Only insert exactly here
        }

        ItemStack ghostStack = inventoryHelper.getStacks()[ghostIndex];
        if (ghostStack == null) {
            return true;
        }
        return ghostStack.isItemEqual(stack);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, int side) {
        if (index < ItemFilterContainer.SLOT_BUFFER) {
            return false;
        }
        if (!isOutputMode(side)) {
            return false;
        }

        int ghostIndex = index - ItemFilterContainer.SLOT_BUFFER;
        if (inputMode[side] == MODE_OUTPUT_EXACT && ghostIndex != side) {
            return false;
        }

        return true;
    }

    private boolean isInputMode(int side) {
        return inputMode[side] > MODE_DISABLED;
    }

    private boolean isOutputMode(int side) {
        return inputMode[side] < MODE_DISABLED;
    }
}
