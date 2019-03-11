package mcjty.rftools.compat.jei;

import mcjty.rftools.blocks.crafter.CrafterBaseTE;
import mcjty.rftools.blocks.crafter.CrafterContainer;
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

public class CrafterRecipeTransferHandler implements IRecipeTransferHandler<CrafterContainer> {

    public static void register(IRecipeTransferRegistry transferRegistry) {
        transferRegistry.addRecipeTransferHandler(new CrafterRecipeTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
    }

    @Override
    public Class<CrafterContainer> getContainerClass() {
        return CrafterContainer.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull CrafterContainer container, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = recipeLayout.getItemStacks().getGuiIngredients();

        CrafterBaseTE inventory = container.getCrafterTE();
        BlockPos pos = inventory.getPos();

        if (doTransfer) {
            RFToolsJeiPlugin.transferRecipe(guiIngredients, pos);
        }

        return null;
    }
}
