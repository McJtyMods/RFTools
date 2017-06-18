package mcjty.rftools.jei;

import mcjty.rftools.blocks.storage.ModularStorageContainer;
import mcjty.rftools.blocks.storage.ModularStorageTileEntity;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class ModularStorageRecipeTransferHandler implements IRecipeTransferHandler {

    public static void register(IRecipeTransferRegistry transferRegistry, IRecipeTransferHandler handler) {
        transferRegistry.addRecipeTransferHandler(handler, VanillaRecipeCategoryUid.CRAFTING);
    }

    @Override
    public Class<? extends Container> getContainerClass() {
        return ModularStorageContainer.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull Container container, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = recipeLayout.getItemStacks().getGuiIngredients();

        ModularStorageContainer containerWorktable = (ModularStorageContainer) container;
        ModularStorageTileEntity te = containerWorktable.getModularStorageTileEntity();
        BlockPos pos = te.getPos();

        if (doTransfer) {
            RFToolsJeiPlugin.transferRecipe(guiIngredients, pos);
        }

        return null;
    }

}
