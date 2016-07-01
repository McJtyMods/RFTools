package mcjty.rftools.craftinggrid;

import net.minecraft.item.ItemStack;

public interface CraftingGridProvider {

    String CMD_GRIDCRAFT = "gridCraft";

    void setRecipe(int index, ItemStack[] stacks);

    CraftingGrid getCraftingGrid();
}
