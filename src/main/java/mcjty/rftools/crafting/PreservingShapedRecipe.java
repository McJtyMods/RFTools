package mcjty.rftools.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

public class PreservingShapedRecipe extends ShapedRecipes {
    private Object objectToInheritFrom;

    // @todo recipes

    public PreservingShapedRecipe(String group, int width, int height, NonNullList<Ingredient> ingredients, ItemStack result, Object objectToInheritFrom) {
        super(group, width, height, ingredients, result);
        this.objectToInheritFrom = objectToInheritFrom;
    }

//    public PreservingShapedRecipe(int width, int height, ItemStack[] items, ItemStack output, int takeNBTFromSlot) {
//        super(width, height, items, output);
//        Item item = items[takeNBTFromSlot].getItem();
//        objectToInheritFrom = getObjectFromStack(item);
//    }

    private Object getObjectFromStack(Item item) {
        if (item instanceof ItemBlock) {
            return ((ItemBlock) item).getBlock();
        } else {
            return item;
        }
    }


    private NBTTagCompound getNBTFromObject(InventoryCrafting inventoryCrafting) {
        for (int i = 0 ; i < inventoryCrafting.getSizeInventory() ; i++) {
            ItemStack stack = inventoryCrafting.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() != null) {
                Object o = getObjectFromStack(stack.getItem());
                if (objectToInheritFrom.equals(o)) {
                    return stack.getTagCompound();
                }
            }
        }
        return null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {
        ItemStack stack = super.getCraftingResult(inventoryCrafting);
        if (!stack.isEmpty()) {
            NBTTagCompound tagCompound = getNBTFromObject(inventoryCrafting);
            if (tagCompound != null) {
                stack.setTagCompound(tagCompound);
            }
        }
        return stack;
    }

}
