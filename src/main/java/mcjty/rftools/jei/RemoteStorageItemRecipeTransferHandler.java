package mcjty.rftools.jei;

import mcjty.rftools.blocks.storage.RemoteStorageItemContainer;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class RemoteStorageItemRecipeTransferHandler implements IRecipeTransferHandler {

    @Override
    public Class<? extends Container> getContainerClass() {
        return RemoteStorageItemContainer.class;
    }

    @Override
    public String getRecipeCategoryUid() {
        return VanillaRecipeCategoryUid.CRAFTING;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull Container container, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = recipeLayout.getItemStacks().getGuiIngredients();

        if (doTransfer) {
            RFToolsJeiPlugin.transferRecipe(guiIngredients, null);
        }

        return null;
    }

}
