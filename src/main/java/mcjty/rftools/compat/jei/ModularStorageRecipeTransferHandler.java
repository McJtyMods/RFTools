package mcjty.rftools.compat.jei;

public class ModularStorageRecipeTransferHandler {} /*implements IRecipeTransferHandler<ModularStorageContainer> {

    public static void register(IRecipeTransferRegistration transferRegistry) {
        transferRegistry.addRecipeTransferHandler(new ModularStorageRecipeTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
    }

    @Override
    public Class<ModularStorageContainer> getContainerClass() {
        return ModularStorageContainer.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull ModularStorageContainer container, @Nonnull IRecipeLayout recipeLayout, @Nonnull PlayerEntity player, boolean maxTransfer, boolean doTransfer) {
        Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = recipeLayout.getItemStacks().getGuiIngredients();

        ModularStorageTileEntity te = container.getModularStorageTileEntity();
        BlockPos pos = te.getPos();

        if (doTransfer) {
            RFToolsJeiPlugin.transferRecipe(guiIngredients, pos);
        }

        return null;
    }

}
*/