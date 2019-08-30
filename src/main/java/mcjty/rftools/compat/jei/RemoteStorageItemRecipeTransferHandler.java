package mcjty.rftools.compat.jei;

public class RemoteStorageItemRecipeTransferHandler {} /*implements IRecipeTransferHandler<RemoteStorageItemContainer> {

    public static void register(IRecipeTransferRegistration transferRegistry) {
        transferRegistry.addRecipeTransferHandler(new RemoteStorageItemRecipeTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
    }

    @Override
    public Class<RemoteStorageItemContainer> getContainerClass() {
        return RemoteStorageItemContainer.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull RemoteStorageItemContainer container, @Nonnull IRecipeLayout recipeLayout, @Nonnull PlayerEntity player, boolean maxTransfer, boolean doTransfer) {
        Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = recipeLayout.getItemStacks().getGuiIngredients();

        if (doTransfer) {
            RFToolsJeiPlugin.transferRecipe(guiIngredients, null);
        }

        return null;
    }

}
*/