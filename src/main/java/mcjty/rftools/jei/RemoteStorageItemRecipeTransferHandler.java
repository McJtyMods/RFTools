package mcjty.rftools.jei;

import mcjty.rftools.blocks.storage.RemoteStorageItemContainer;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class RemoteStorageItemRecipeTransferHandler implements IRecipeTransferHandler<RemoteStorageItemContainer> {

    public static void register(IRecipeTransferRegistry transferRegistry, IRecipeTransferHandler handler) {
        transferRegistry.addRecipeTransferHandler(handler, VanillaRecipeCategoryUid.CRAFTING);
    }

    @Override
    public Class<RemoteStorageItemContainer> getContainerClass() {
        return RemoteStorageItemContainer.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull RemoteStorageItemContainer container, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = recipeLayout.getItemStacks().getGuiIngredients();

        if (doTransfer) {
            RFToolsJeiPlugin.transferRecipe(guiIngredients, null);
        }

        return null;
    }

}
