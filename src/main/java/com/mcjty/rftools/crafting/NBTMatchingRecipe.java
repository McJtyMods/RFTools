package com.mcjty.rftools.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class NBTMatchingRecipe extends ShapedRecipes {

    public NBTMatchingRecipe(int width, int height, ItemStack[] input, ItemStack output) {
        super(width, height, input, output);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {
        return super.getCraftingResult(inventoryCrafting);
    }

    @Override
    public boolean matches(InventoryCrafting inventoryCrafting, World world) {
        for (int i = 0; i <= 3 - this.recipeWidth; ++i) {
            for (int j = 0; j <= 3 - this.recipeHeight; ++j) {
                if (checkMatch(inventoryCrafting, i, j, true)) {
                    return true;
                }

                if (checkMatch(inventoryCrafting, i, j, false)) {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * Checks if the region of a crafting inventory is match for the recipe.
     */
    private boolean checkMatch(InventoryCrafting inventoryCrafting, int p_77573_2_, int p_77573_3_, boolean p_77573_4_) {
        for (int k = 0; k < 3; ++k) {
            for (int l = 0; l < 3; ++l) {
                int i1 = k - p_77573_2_;
                int j1 = l - p_77573_3_;
                ItemStack itemstack = null;

                if (i1 >= 0 && j1 >= 0 && i1 < this.recipeWidth && j1 < this.recipeHeight) {
                    if (p_77573_4_) {
                        itemstack = this.recipeItems[this.recipeWidth - i1 - 1 + j1 * this.recipeWidth];
                    } else {
                        itemstack = this.recipeItems[i1 + j1 * this.recipeWidth];
                    }
                }

                ItemStack itemstack1 = inventoryCrafting.getStackInRowAndColumn(k, l);

                if (itemstack1 != null || itemstack != null) {
                    if (itemstack1 == null || itemstack == null) {
                        return false;
                    }

                    if (itemstack.getItem() != itemstack1.getItem()) {
                        return false;
                    }

                    if (itemstack.getItemDamage() != 32767 && itemstack.getItemDamage() != itemstack1.getItemDamage()) {
                        return false;
                    }

                    NBTTagCompound compound = itemstack.getTagCompound();
                    NBTTagCompound compound1 = itemstack1.getTagCompound();
                    if (compound == null && compound1 != null) {
                        return false;
                    }
                    if (compound != null && compound1 == null) {
                        return false;
                    }

                    if (compound != null && !compound.equals(compound1)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
