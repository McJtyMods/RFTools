package com.mcjty.rftools.blocks.crafter;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

public class CraftingRecipe {
    private ItemStack stacks[] = new ItemStack[10];

    public void readFromNBT(NBTTagCompound tagCompound) {
        NBTTagList nbtTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < nbtTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = nbtTagList.getCompoundTagAt(i);
            stacks[i] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
        }
    }

    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList nbtTagList = new NBTTagList();
        for (ItemStack stack : stacks) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            nbtTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", nbtTagList);
    }

    public void setRecipe(ItemStack[] items) {
        stacks = items;
    }

    public ItemStack getItemStack(int index) {
        return stacks[index];
    }
}
