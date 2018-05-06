package mcjty.rftools.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public class NBTMatchingRecipe extends ShapedRecipes {

    private final String[][] matchingNBTs;

    public NBTMatchingRecipe(int width, int height, ItemStack[] input, String[][] matchingNBTs, ItemStack output) {
        super("rftools:nbtmatching", width, height, getIngredients(input), output);
        this.matchingNBTs = matchingNBTs;
    }

    private static NonNullList<Ingredient> getIngredients(ItemStack[] input) {
        NonNullList<Ingredient> inputList = NonNullList.withSize(input.length, Ingredient.EMPTY);
        for (int i = 0 ; i < input.length ; i++) {
            inputList.set(i, Ingredient.fromStacks(input[i]));
        }
        return inputList;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {
        return super.getCraftingResult(inventoryCrafting);
    }

    @Override
    public boolean matches(InventoryCrafting inventoryCrafting, World world) {
        for (int i = 0; i <= 3 - this.recipeWidth; ++i) {
            for (int j = 0; j <= 3 - this.recipeHeight; ++j) {
                if (checkMatchNBT(inventoryCrafting, i, j, true)) {
                    return true;
                }

                if (checkMatchNBT(inventoryCrafting, i, j, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if the region of a crafting inventory is match for the recipe.
     */
    private boolean checkMatchNBT(InventoryCrafting inventoryCrafting, int x, int y, boolean reversed) {
        for (int col = 0; col < 3; ++col) {
            for (int row = 0; row < 3; ++row) {
                int i1 = col - x;
                int j1 = row - y;
                ItemStack itemstack = ItemStack.EMPTY;
                String[] nbt = null;

                if (i1 >= 0 && j1 >= 0 && i1 < this.recipeWidth && j1 < this.recipeHeight) {
                    int idx;
                    if (reversed) {
                        idx = this.recipeWidth - i1 - 1 + j1 * this.recipeWidth;
                    } else {
                        idx = i1 + j1 * this.recipeWidth;
                    }
                    Ingredient ingredient = this.recipeItems.get(idx);
                    if (ingredient.getMatchingStacks().length > 0) {
                        itemstack = ingredient.getMatchingStacks()[0]; // @todo recipes most likely wrong!
                    } else {
                        itemstack = ItemStack.EMPTY;
                    }
                    nbt = this.matchingNBTs[idx];
                }

                ItemStack itemstack1 = inventoryCrafting.getStackInRowAndColumn(col, row);

                if (!itemstack1.isEmpty() || !itemstack.isEmpty()) {
                    if (itemstack1.isEmpty() || itemstack.isEmpty()) {
                        return false;
                    }

                    if (itemstack.getItem() != itemstack1.getItem()) {
                        return false;
                    }

                    if (itemstack.getMetadata() != 32767 && itemstack.getMetadata() != itemstack1.getMetadata()) {
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
