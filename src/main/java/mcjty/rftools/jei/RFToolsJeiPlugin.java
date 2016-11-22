package mcjty.rftools.jei;

import mcjty.rftools.network.RFToolsMessages;
import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JEIPlugin
public class RFToolsJeiPlugin extends BlankModPlugin {

    public static void transferRecipe(Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients, BlockPos pos) {
        List<ItemStack> items = new ArrayList<>(10);
        for (int i = 0 ; i < 10 ; i++) {
            items.add(null);
        }
        for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : guiIngredients.entrySet()) {
            int recipeSlot = entry.getKey();
            List<ItemStack> allIngredients = entry.getValue().getAllIngredients();
            if (!allIngredients.isEmpty()) {
                items.set(recipeSlot, allIngredients.get(0));
            }
        }

        RFToolsMessages.INSTANCE.sendToServer(new PacketSendRecipe(items, pos));
    }

    @Override
    public void register(@Nonnull IModRegistry registry) {
        IRecipeTransferRegistry transferRegistry = registry.getRecipeTransferRegistry();
        // @todo @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        transferRegistry.addRecipeTransferHandler(new CrafterRecipeTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
        transferRegistry.addRecipeTransferHandler(new ModularStorageRecipeTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
        transferRegistry.addRecipeTransferHandler(new ModularStorageItemRecipeTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
        transferRegistry.addRecipeTransferHandler(new RemoteStorageItemRecipeTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
        transferRegistry.addRecipeTransferHandler(new StorageScannerRecipeTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
    }
}
