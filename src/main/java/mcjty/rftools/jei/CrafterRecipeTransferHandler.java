package mcjty.rftools.jei;

import mcjty.lib.jei.CompatRecipeTransferHandler;
import mcjty.rftools.blocks.crafter.CrafterBaseTE;
import mcjty.rftools.blocks.crafter.CrafterContainer;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class CrafterRecipeTransferHandler implements CompatRecipeTransferHandler {

    @Override
    public Class<? extends Container> getContainerClass() {
        return CrafterContainer.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull Container container, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = recipeLayout.getItemStacks().getGuiIngredients();

        CrafterContainer containerWorktable = (CrafterContainer) container;
        IInventory inventory = containerWorktable.getCrafterTE();
        BlockPos pos = ((CrafterBaseTE) inventory).getPos();

        if (doTransfer) {
            RFToolsJeiPlugin.transferRecipe(guiIngredients, pos);
        }

        return null;
    }
}
