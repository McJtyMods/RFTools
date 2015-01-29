package com.mcjty.rftools.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;

public class PreservingShapedRecipe extends ShapedRecipes {
    private int takeNBTFromSlot;

    public PreservingShapedRecipe(int width, int height, ItemStack[] items, ItemStack output, int takeNBTFromSlot) {
        super(width, height, items, output);
        this.takeNBTFromSlot = takeNBTFromSlot;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {
        ItemStack stack = super.getCraftingResult(inventoryCrafting);
        if (stack != null) {
            NBTTagCompound tagCompound = inventoryCrafting.getStackInSlot(takeNBTFromSlot).getTagCompound();
            if (tagCompound != null) {
                stack.setTagCompound(tagCompound);
            }
        }
        return stack;
    }

}
