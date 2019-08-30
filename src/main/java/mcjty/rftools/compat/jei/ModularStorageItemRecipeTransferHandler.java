package mcjty.rftools.compat.jei;

public class ModularStorageItemRecipeTransferHandler {}/*implements IRecipeTransferHandler<ModularStorageItemContainer> {

    public static void register(IRecipeTransferRegistration transferRegistry) {
        transferRegistry.addRecipeTransferHandler(new ModularStorageItemRecipeTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
    }

    @Override
    public Class<ModularStorageItemContainer> getContainerClass() {
        return ModularStorageItemContainer.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull ModularStorageItemContainer container, @Nonnull IRecipeLayout recipeLayout, @Nonnull PlayerEntity player, boolean maxTransfer, boolean doTransfer) {
        Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = recipeLayout.getItemStacks().getGuiIngredients();

        if (doTransfer) {
            RFToolsJeiPlugin.transferRecipe(guiIngredients, null);
        }

        return null;
    }

}
*/