package mcjty.rftools.craftinggrid;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface CraftingGridProvider {

    void setRecipe(int index, ItemStack[] stacks);

    void storeRecipe(int index);

    CraftingGrid getCraftingGrid();

    void markInventoryDirty();

    @Nonnull
    int[] craft(EntityPlayer player, int n, boolean test);
}
