package mcjty.rftools.crafting;

public class ContainerToItemRecipeFactory {} /* @todo 1.14 implements IRecipeFactory {
    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        ShapedOreRecipe recipe = ShapedOreRecipe.factory(context, json);

        ShapedPrimer primer = new ShapedPrimer();
        primer.width = recipe.getRecipeWidth();
        primer.height = recipe.getRecipeHeight();
        primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true);
        primer.input = recipe.getIngredients();

        return new ContainerToItemRecipe(new ResourceLocation(RFTools.MODID, "container_to_item"), recipe.getRecipeOutput(), primer);
    }

    public static class ContainerToItemRecipe extends ShapedOreRecipe {
        public ContainerToItemRecipe(ResourceLocation group, ItemStack result, ShapedPrimer primer) {
            super(group, result, primer);
        }


        @Override
        public ItemStack getCraftingResult(CraftingInventory inventoryCrafting) {
            ItemStack result = super.getCraftingResult(inventoryCrafting);
            if (!result.isEmpty()) {

                ItemStack tabletItem = ItemStack.EMPTY;

                for (int i = 0; i < inventoryCrafting.getSizeInventory(); ++i) {
                    ItemStack stack = inventoryCrafting.getStackInSlot(i);

                    if (!stack.isEmpty()) {
                        if (stack.getItem() instanceof StorageModuleTabletItem) {
                            tabletItem = stack;
                        }
                    }
                }

                CompoundNBT tagCompound = null;
                if (!tabletItem.isEmpty()) {
                    tagCompound = tabletItem.getTag();
                }

                int childDamage = 0;
                CompoundNBT newtag = new CompoundNBT();
                if (tagCompound != null) {
                    for (Object o : tagCompound.getKeySet()) {
                        String tag = (String) o;
                        // @todo add a list of blacklisted NBT tags (make this more general)
                        if ((!"childDamage".equals(tag)) && (!"Energy".equals(tag)) && (!"grid".equals(tag))) {
                            newtag.setTag(tag, tagCompound.getTag(tag));
                        }
                    }

                    childDamage = tagCompound.getInt("childDamage");
                    if (childDamage == META_FOR_SCANNER) {
                        childDamage = 0;
                    }
                }
                result.setItemDamage(childDamage);
                result.setTagCompound(newtag);
            }
            return result;
        }
    }
}*/