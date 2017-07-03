package mcjty.rftools.crafting;

import com.google.gson.JsonObject;
import mcjty.lib.crafting.INBTPreservingIngredient;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.screenmodules.StorageControlModuleItem;
import mcjty.rftools.items.storage.StorageModuleTabletItem;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper.ShapedPrimer;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;

import static mcjty.rftools.items.storage.StorageModuleTabletItem.META_FOR_SCANNER;

public class ContainerAndItemRecipeFactory implements IRecipeFactory {
    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        ShapedOreRecipe recipe = ShapedOreRecipe.factory(context, json);

        ShapedPrimer primer = new ShapedPrimer();
        primer.width = recipe.getWidth();
        primer.height = recipe.getHeight();
        primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true);
        primer.input = recipe.getIngredients();

        return new ContainerAndItemRecipe(new ResourceLocation(RFTools.MODID, "container_and_item"), recipe.getRecipeOutput(), primer);
    }

    public static class ContainerAndItemRecipe extends ShapedOreRecipe {
        public ContainerAndItemRecipe(ResourceLocation group, ItemStack result, ShapedPrimer primer) {
            super(group, result, primer);
        }

        @Override
        public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {
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

                NBTTagCompound tagCompound = null;
                if (!itemstackForNBT.isEmpty()) {
                    tagCompound = itemstackForNBT.getTagCompound();
                    if (tagCompound != null) {
                        tagCompound = tagCompound.copy();
                    }
                }
                if (tagCompound != null) {
                    result.setTagCompound(tagCompound);
                }

                NBTTagCompound tagCompoundTablet = null;
                if (!tabletItem.isEmpty()) {
                    tagCompoundTablet = tabletItem.getTagCompound();
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

        private static NBTTagCompound getCompound(ItemStack stack) {
            if (stack.getTagCompound() == null) {
                stack.setTagCompound(new NBTTagCompound());
            }
            return stack.getTagCompound();
        }
    }
}