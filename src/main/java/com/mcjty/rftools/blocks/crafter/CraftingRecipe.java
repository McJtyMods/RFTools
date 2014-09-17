package com.mcjty.rftools.blocks.crafter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class CraftingRecipe {
    private InventoryCrafting inv = new InventoryCrafting(new Container() {
        @Override
        public boolean canInteractWith(EntityPlayer var1) {
            return false;
        }
    }, 3, 3);
    private ItemStack result;

    private boolean keepOne = false;
    private boolean craftInternal = false;

    public IRecipe findRecipe(World world) {
        List recipes = CraftingManager.getInstance().getRecipeList();
        for (Object recipe : recipes) {
            IRecipe irecipe = (IRecipe) recipe;

            if (irecipe.matches(inv, world)) {
                return irecipe;
            }
        }
        return null;
    }

    public void readFromNBT(NBTTagCompound tagCompound) {
        NBTTagList nbtTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < nbtTagList.tagCount(); i++) {
            NBTTagCompound nbtTagCompound = nbtTagList.getCompoundTagAt(i);
            inv.setInventorySlotContents(i, ItemStack.loadItemStackFromNBT(nbtTagCompound));
        }
        NBTTagCompound resultCompound = tagCompound.getCompoundTag("Result");
        if (resultCompound != null) {
            result = ItemStack.loadItemStackFromNBT(resultCompound);
        } else {
            result = null;
        }
        keepOne = tagCompound.getBoolean("Keep");
        craftInternal = tagCompound.getBoolean("Int");
    }

    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList nbtTagList = new NBTTagList();
        for (int i = 0 ; i < 9 ; i++) {
            ItemStack stack = inv.getStackInSlot(i);
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            nbtTagList.appendTag(nbtTagCompound);
        }
        NBTTagCompound resultCompound = new NBTTagCompound();
        if (result != null) {
            result.writeToNBT(resultCompound);
        }
        tagCompound.setTag("Result", resultCompound);
        tagCompound.setTag("Items", nbtTagList);
        tagCompound.setBoolean("Keep", keepOne);
        tagCompound.setBoolean("Int", craftInternal);
    }

    public void setRecipe(InventoryCrafting inv, ItemStack result) {
        this.inv = inv;
        this.result = result;
    }

    public void setRecipe(ItemStack[] items, ItemStack result) {
        for (int i = 0 ; i < 9 ; i++) {
            inv.setInventorySlotContents(i, items[i]);
        }
        this.result = result;
    }

    public InventoryCrafting getInventory() {
        return inv;
    }

    public void setResult(ItemStack result) {
        this.result = result;
    }

    public ItemStack getResult() {
        return result;
    }

    public boolean isKeepOne() {
        return keepOne;
    }

    public void setKeepOne(boolean keepOne) {
        this.keepOne = keepOne;
    }

    public boolean isCraftInternal() {
        return craftInternal;
    }

    public void setCraftInternal(boolean craftInternal) {
        this.craftInternal = craftInternal;
    }
}
