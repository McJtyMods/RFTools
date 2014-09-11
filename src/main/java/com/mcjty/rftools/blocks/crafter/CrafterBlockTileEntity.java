package com.mcjty.rftools.blocks.crafter;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class CrafterBlockTileEntity extends GenericEnergyHandlerTileEntity implements ISidedInventory {
    private ItemStack stacks[] = new ItemStack[10 + CrafterContainerFactory.BUFFER_SIZE + CrafterContainerFactory.BUFFEROUT_SIZE];

    public static final int MAXENERGY = 32000;

    public CrafterBlockTileEntity() {
        super(MAXENERGY, 80);
    }

    @Override
    public int getSizeInventory() {
        return stacks.length;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return stacks[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        if (CrafterContainerFactory.getInstance().isGhostSlot(index)) {
            ItemStack old = stacks[index];
            stacks[index] = null;
            if (old == null) {
                return null;
            }
            old.stackSize = 0;
            return old;
        } else {
            if (stacks[index] != null) {
                if (stacks[index].stackSize <= amount) {
                    ItemStack old = stacks[index];
                    stacks[index] = null;
                    markDirty();
                    return old;
                }
                ItemStack its = stacks[index].splitStack(amount);
                if (stacks[index].stackSize == 0) {
                    stacks[index] = null;
                }
                markDirty();
                return its;
            }
            return null;
        }
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (CrafterContainerFactory.getInstance().isGhostSlot(index)) {
            if (stack != null) {
                stacks[index] = stack.copy();
                if (index < 9) {
                    stacks[index].stackSize = 0;
                }
            } else {
                stacks[index] = null;
            }
        } else {
            stacks[index] = stack;
            if (stack != null && stack.stackSize > getInventoryStackLimit()) {
                stack.stackSize = getInventoryStackLimit();
            }
            markDirty();
        }
    }

    @Override
    public String getInventoryName() {
        return "Crafter Inventory";
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
    public int[] getAccessibleSlotsFromSide(int side) {
        return CrafterContainerFactory.getInstance().getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack item, int side) {
        return CrafterContainerFactory.getInstance().isInputSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack item, int side) {
        return CrafterContainerFactory.getInstance().isOutputSlot(index);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);

//        NBTTagList nbtTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
//        stacks = null;
//        if (nbtTagList.tagCount() > 0) {
//            NBTTagCompound nbtTagCompound = nbtTagList.getCompoundTagAt(0);
//            stacks = ItemStack.loadItemStackFromNBT(nbtTagCompound);
//        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
//        NBTTagList nbtTagList = new NBTTagList();
//        if (stacks != null) {
//            NBTTagCompound nbtTagCompound = new NBTTagCompound();
//            stacks.writeToNBT(nbtTagCompound);
//            nbtTagList.appendTag(nbtTagCompound);
//        }
//        tagCompound.setTag("Items", nbtTagList);
    }

    @Override
    public boolean canUpdate() {
        // We currently don't need updates for this tile entity yet
        return false;
    }
}
