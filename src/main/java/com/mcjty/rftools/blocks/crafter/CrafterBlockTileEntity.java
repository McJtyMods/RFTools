package com.mcjty.rftools.blocks.crafter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

public class CrafterBlockTileEntity extends TileEntity implements IInventory {
    private ItemStack stacks[] = new ItemStack[10];

    public CrafterBlockTileEntity() { }

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
        ItemStack old = stacks[index];
        stacks[index] = null;
        if (old == null) {
            return null;
        }
        old.stackSize = 0;
        return old;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (stack != null) {
            stacks[index] = stack.copy();
            if (index < 9) {
                stacks[index].stackSize = 0;
            }
        } else {
            stacks[index] = null;
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
        return 0;
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
