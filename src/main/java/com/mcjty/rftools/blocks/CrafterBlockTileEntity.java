package com.mcjty.rftools.blocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

public class CrafterBlockTileEntity extends TileEntity implements IInventory {
    public ItemStack stacks[] = new ItemStack[10];

    public CrafterBlockTileEntity() { }

    @Override
    public int getSizeInventory() {
        return 10;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return stacks[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        if (stacks != null) {
            if (stacks[index].stackSize <= amount) {
                ItemStack old = stacks[index];
                stacks = null;
                markDirty();
                return old;
            }
            ItemStack its = stacks[index].splitStack(amount);
            if (stacks[index].stackSize == 0) {
                stacks = null;
            }
            markDirty();
            return its;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        ItemStack old = stacks[index];
        setInventorySlotContents(index, null);
        return old;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack itemStack) {
        stacks[index] = itemStack;
        if (itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
            itemStack.stackSize = getInventoryStackLimit();
        }
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "Crafter Inventory";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return true;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer p_70300_1_) {
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

}
