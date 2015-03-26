package com.mcjty.rftools.blocks.itemfilter;

import com.mcjty.container.InventoryHelper;
import com.mcjty.entity.GenericTileEntity;
import com.mcjty.rftools.items.dimlets.DimletType;
import com.mcjty.rftools.network.Argument;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.Map;

public class DimletFilterTileEntity extends GenericTileEntity implements ISidedInventory {
    public static final String CMD_SETMODE = "setMode";
    public static final String CMD_SETMINRARITY = "setMinRarity";
    public static final String CMD_SETMAXRARITY = "setMaxRarity";
    public static final String CMD_SETTYPE = "setType";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, DimletFilterContainer.factory, DimletFilterContainer.BUFFER_SIZE);

    public static final int DISABLED = 0;
    public static final int INPUT = 1;
    public static final int OUTPUT = 2;

    private int inputMode[] = new int[6];
    private int minRarity[] = new int[6];
    private int maxRarity[] = new int[6];
    private DimletType types[] = new DimletType[6];

    public int[] getInputMode() {
        return inputMode;
    }

    public int[] getMinRarity() {
        return minRarity;
    }

    public int[] getMaxRarity() {
        return maxRarity;
    }

    public DimletType[] getTypes() {
        return types;
    }

    public DimletFilterTileEntity() {
        for (int i = 0 ; i < 6 ; i++) {
            inputMode[i] = DISABLED;
            minRarity[i] = 0;
            maxRarity[i] = 6;
            types[i] = null;
        }
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
        inputMode = tagCompound.getIntArray("inputs");
        minRarity = tagCompound.getIntArray("minRarity");
        if (minRarity == null || minRarity.length == 0) {
            minRarity = new int[6];
        }
        maxRarity = tagCompound.getIntArray("maxRarity");
        if (maxRarity == null || maxRarity.length == 0) {
            maxRarity = new int[6];
        }
        int[] typesI = tagCompound.getIntArray("types");
        if (typesI == null || typesI.length == 0) {
            for (int i = 0 ; i < 6 ; i++) {
                types[i] = null;
            }
        } else {
            for (int i = 0; i < 6; i++) {
                if (typesI[i] == -1) {
                    types[i] = null;
                } else {
                    types[i] = DimletType.values()[typesI[i]];
                }
            }
        }
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
        tagCompound.setIntArray("inputs", inputMode);
        tagCompound.setIntArray("minRarity", minRarity);
        tagCompound.setIntArray("maxRarity", maxRarity);
        int[] typesI = new int[6];
        for (int i = 0 ; i < 6 ; i++) {
            typesI[i] = types[i] == null ? -1 : types[i].ordinal();
        }
        tagCompound.setIntArray("types", typesI);
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
            Integer side = args.get("side").getInteger();
            Integer input = args.get("input").getInteger();

            inputMode[side] = input;
            markDirty();
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            return true;
        } else if (CMD_SETMINRARITY.equals(command)) {
            Integer side = args.get("side").getInteger();
            Integer value = args.get("value").getInteger();

            minRarity[side] = value;
            markDirty();
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            return true;
        } else if (CMD_SETMAXRARITY.equals(command)) {
            Integer side = args.get("side").getInteger();
            Integer value = args.get("value").getInteger();

            maxRarity[side] = value;
            markDirty();
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            return true;
        } else if (CMD_SETTYPE.equals(command)) {
            Integer side = args.get("side").getInteger();
            Integer type = args.get("type").getInteger();

            if (type == -1) {
                types[side] = null;
            } else {
                types[side] = DimletType.values()[type];
            }
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
        return "Dimlet Filter Inventory";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 16;
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
    public int[] getAccessibleSlotsFromSide(int side) {
        int v = DimletFilterContainer.SLOT_BUFFER;
        return new int[] { v, v+1, v+2, v+3, v+4, v+5, v+6, v+7, v+8 };
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, int side) {
        return isInputMode(side);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, int side) {
        if (!isOutputMode(side)) {
            return false;
        }
        // Do filter matching here ! @@@
        return true;
    }

    private boolean isInputMode(int side) {
        return inputMode[side] == INPUT;
    }

    private boolean isOutputMode(int side) {
        return inputMode[side] == OUTPUT;
    }
}
