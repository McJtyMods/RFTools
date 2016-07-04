package mcjty.rftools.craftinggrid;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public interface CraftingGridProvider {

    void setRecipe(int index, ItemStack[] stacks);

    void storeRecipe(int index);

    CraftingGrid getCraftingGrid();

    void markDirty();

    int[] craft(EntityPlayerMP player, int n, boolean test);
}
