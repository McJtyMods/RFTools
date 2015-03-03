package com.mcjty.rftools.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class NBTMatchingRecipe extends ShapedRecipes {

    private final String[][] matchingNBTs;

    public NBTMatchingRecipe(int width, int height, ItemStack[] input, String[][] matchingNBTs, ItemStack output) {
        super(width, height, input, output);
        this.matchingNBTs = matchingNBTs;
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
    private boolean checkMatch(InventoryCrafting inventoryCrafting, int x, int y, boolean reversed) {
        for (int col = 0; col < 3; ++col) {
            for (int row = 0; row < 3; ++row) {
                int i1 = col - x;
                int j1 = row - y;
                ItemStack itemstack = null;
                String[] nbt = null;

                if (i1 >= 0 && j1 >= 0 && i1 < this.recipeWidth && j1 < this.recipeHeight) {
                    int idx;
                    if (reversed) {
                        idx = this.recipeWidth - i1 - 1 + j1 * this.recipeWidth;
                    } else {
                        idx = i1 + j1 * this.recipeWidth;
                    }
                    itemstack = this.recipeItems[idx];
                    nbt = this.matchingNBTs[idx];
                }

                ItemStack itemstack1 = inventoryCrafting.getStackInRowAndColumn(col, row);

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
                    if (nbt != null) {
                        if (compound == null && compound1 != null) {
                            return false;
                        }
                        if (compound != null && compound1 == null) {
                            return false;
                        }
                        if (compound != null) {
                            for (String tagName : nbt) {
                                NBTBase tag = compound.getTag(tagName);
                                NBTBase tag1 = compound1.getTag(tagName);
                                if (tag == null && tag1 != null) {
                                    return false;
                                }
                                if (tag != null && tag1 == null) {
                                    return false;
                                }
                                if (tag != null) {
                                    if (!tag.equals(tag1)) {
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }
}
