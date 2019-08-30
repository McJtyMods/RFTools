package mcjty.rftools.compat.jei;

public class StorageScannerRecipeTransferHandler {} /*implements IRecipeTransferHandler<StorageScannerContainer> {

    public static void register(IRecipeTransferRegistration transferRegistry) {
        transferRegistry.addRecipeTransferHandler(new StorageScannerRecipeTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
    }

    @Override
    public Class<StorageScannerContainer> getContainerClass() {
        return StorageScannerContainer.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull StorageScannerContainer container, @Nonnull IRecipeLayout recipeLayout, @Nonnull PlayerEntity player, boolean maxTransfer, boolean doTransfer) {
        Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = recipeLayout.getItemStacks().getGuiIngredients();

        StorageScannerTileEntity te = container.getStorageScannerTileEntity();
        BlockPos pos = te.getCraftingGridContainerPos();

        if (doTransfer) {
            RFToolsJeiPlugin.transferRecipe(guiIngredients, pos);
        }

        return null;
    }

}
*/