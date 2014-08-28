package com.mcjty.rftools.blocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

public class CrafterBlockTileEntity extends TileEntity implements IInventory {
    public ItemStack singlestack;

    public CrafterBlockTileEntity() {
        singlestack = null;
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return singlestack;
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        if (singlestack != null) {
            if (singlestack.stackSize <= amount) {
                ItemStack old = singlestack;
                singlestack = null;
                markDirty();
                return old;
            }
            ItemStack its = singlestack.splitStack(amount);
            if (singlestack.stackSize == 0) {
                singlestack = null;
            }
            markDirty();
            return its;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        ItemStack old = singlestack;
        setInventorySlotContents(index, null);
        return old;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack itemStack) {
        singlestack = itemStack;
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
        NBTTagList nbtTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        singlestack = null;
        if (nbtTagList.tagCount() > 0) {
            NBTTagCompound nbtTagCompound = nbtTagList.getCompoundTagAt(0);
            singlestack = ItemStack.loadItemStackFromNBT(nbtTagCompound);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        NBTTagList nbtTagList = new NBTTagList();
        if (singlestack != null) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            singlestack.writeToNBT(nbtTagCompound);
            nbtTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", nbtTagList);
    }

}
