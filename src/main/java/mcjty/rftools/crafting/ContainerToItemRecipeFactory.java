package mcjty.rftools.crafting;

import com.google.gson.JsonObject;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.storage.StorageModuleTabletItem;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper.ShapedPrimer;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;

import static mcjty.rftools.items.storage.StorageModuleTabletItem.META_FOR_SCANNER;

public class ContainerToItemRecipeFactory implements IRecipeFactory {
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
        public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {
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

                    childDamage = tagCompound.getInteger("childDamage");
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
}