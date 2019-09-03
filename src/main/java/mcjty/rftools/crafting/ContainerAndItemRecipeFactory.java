package mcjty.rftools.crafting;

public class ContainerAndItemRecipeFactory {} /* @todo 1.14 implements IRecipeFactory {
    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        ShapedOreRecipe recipe = ShapedOreRecipe.factory(context, json);

        ShapedPrimer primer = new ShapedPrimer();
        primer.width = recipe.getRecipeWidth();
        primer.height = recipe.getRecipeHeight();
        primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true);
        primer.input = recipe.getIngredients();

        return new ContainerAndItemRecipe(new ResourceLocation(RFTools.MODID, "container_and_item"), recipe.getRecipeOutput(), primer);
    }

    public static class ContainerAndItemRecipe extends ShapedOreRecipe {
        public ContainerAndItemRecipe(ResourceLocation group, ItemStack result, ShapedPrimer primer) {
            super(group, result, primer);
        }

        @Override
        public ItemStack getCraftingResult(CraftingInventory inventoryCrafting) {
            ItemStack result = super.getCraftingResult(inventoryCrafting);
            if (!result.isEmpty()) {

                ItemStack itemstackForNBT = ItemStack.EMPTY;
                ItemStack tabletItem = ItemStack.EMPTY;

                for (int i = 0; i < inventoryCrafting.getSizeInventory(); ++i) {
                    ItemStack stack = inventoryCrafting.getStackInSlot(i);

                    if (!stack.isEmpty()) {
                        if (stack.getItem() instanceof StorageModuleTabletItem) {
                            tabletItem = stack;
                        } else if (stack.getItem() instanceof INBTPreservingIngredient) {
                            itemstackForNBT = stack;
                        } else if (Block.getBlockFromItem(stack.getItem()) instanceof INBTPreservingIngredient) {
                            itemstackForNBT = stack;
                        }
                    }
                }

                CompoundNBT tagCompound = null;
                if (!itemstackForNBT.isEmpty()) {
                    tagCompound = itemstackForNBT.getTag();
                    if (tagCompound != null) {
                        tagCompound = tagCompound.copy();
                    }
                }
                if (tagCompound != null) {
                    result.setTagCompound(tagCompound);
                }

                CompoundNBT tagCompoundTablet = null;
                if (!tabletItem.isEmpty()) {
                    tagCompoundTablet = tabletItem.getTag();
                }
                if (tagCompoundTablet != null) {
                    getCompound(result).setInteger("Energy", tagCompoundTablet.getInteger("Energy"));
                    // @todo hardcoded!
                    if (tagCompoundTablet.hasKey("grid")) {
                        getCompound(result).setTag("grid", tagCompoundTablet.getTag("grid"));
                    }
                }

                Integer damage;
                if (itemstackForNBT.getItem() instanceof StorageControlModuleItem) {
                    damage = META_FOR_SCANNER;
                } else {
                    damage = itemstackForNBT.getMetadata();
                }
                if (damage != null) {
                    getCompound(result).setInteger("childDamage", damage);
                }
            }
            return result;
        }

        private static CompoundNBT getCompound(ItemStack stack) {
            if (stack.getTag() == null) {
                stack.setTagCompound(new CompoundNBT());
            }
            return stack.getTag();
        }
    }
}
*/