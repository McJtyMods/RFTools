package mcjty.rftools.jei;

import mcjty.rftools.blocks.storagemonitor.StorageScannerContainer;
import mcjty.rftools.blocks.storagemonitor.StorageScannerTileEntity;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class StorageScannerRecipeTransferHandler implements IRecipeTransferHandler<StorageScannerContainer> {

    public static void register(IRecipeTransferRegistry transferRegistry) {
        transferRegistry.addRecipeTransferHandler(new StorageScannerRecipeTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
    }

    @Override
    public Class<StorageScannerContainer> getContainerClass() {
        return StorageScannerContainer.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull StorageScannerContainer container, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = recipeLayout.getItemStacks().getGuiIngredients();

        StorageScannerTileEntity te = container.getStorageScannerTileEntity();
        BlockPos pos = te.getCraftingGridContainerPos();

        if (doTransfer) {
            RFToolsJeiPlugin.transferRecipe(guiIngredients, pos);
        }

        return null;
    }

}
