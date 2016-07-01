package mcjty.rftools.craftinggrid;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public interface CraftingGridProvider {

    void setRecipe(int index, ItemStack[] stacks);

    CraftingGrid getCraftingGrid();

    void markDirty();

    void craft(EntityPlayerMP player, int n);
}
