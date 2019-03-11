package mcjty.rftools.compat.jei;

import net.minecraft.item.ItemStack;

import java.util.List;

public interface JEIRecipeAcceptor {

    void setGridContents(List<ItemStack> stacks);
}
