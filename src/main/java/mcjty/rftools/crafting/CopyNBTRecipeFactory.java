package mcjty.rftools.crafting;

import com.google.gson.JsonObject;
import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper.ShapedPrimer;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;

public class CopyNBTRecipeFactory implements IRecipeFactory {
    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        ShapedOreRecipe recipe = ShapedOreRecipe.factory(context, json);

        ShapedPrimer primer = new ShapedPrimer();
        primer.width = recipe.getWidth();
        primer.height = recipe.getHeight();
        primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true);
        primer.input = recipe.getIngredients();

        return new CopyNBTRecipe(new ResourceLocation(RFTools.MODID, "copy_nbt_crafting"), recipe.getRecipeOutput(), primer);
    }

    public static class CopyNBTRecipe extends ShapedOreRecipe {
        public CopyNBTRecipe(ResourceLocation group, ItemStack result, ShapedPrimer primer) {
            super(group, result, primer);
        }

        @Override
        @Nonnull
        public ItemStack getCraftingResult(@Nonnull InventoryCrafting var1) {
            ItemStack newOutput = this.output.copy();

            ItemStack itemstack = ItemStack.EMPTY;

            for (int i = 0; i < var1.getSizeInventory(); ++i) {
                ItemStack stack = var1.getStackInSlot(i);

                if (!stack.isEmpty()) {
                    if (stack.getItem() instanceof INBTPreservingIngredient) {
                        itemstack = stack;
                    } else if (Block.getBlockFromItem(stack.getItem()) instanceof INBTPreservingIngredient) {
                        itemstack = stack;
                    }
                }
            }

            if (itemstack.hasTagCompound()) {
                newOutput.setTagCompound(itemstack.getTagCompound().copy());
            }

            return newOutput;
        }
    }
}