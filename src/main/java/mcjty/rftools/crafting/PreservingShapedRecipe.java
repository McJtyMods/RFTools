package mcjty.rftools.crafting;

import mcjty.lib.tools.ItemStackTools;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;

public class PreservingShapedRecipe extends ShapedRecipes {
    private Object objectToInheritFrom;

    public PreservingShapedRecipe(int width, int height, ItemStack[] items, ItemStack output, int takeNBTFromSlot) {
        super(width, height, items, output);
        Item item = items[takeNBTFromSlot].getItem();
        objectToInheritFrom = getObjectFromStack(item);
    }

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
            if (ItemStackTools.isValid(stack) && stack.getItem() != null) {
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
        if (ItemStackTools.isValid(stack)) {
            NBTTagCompound tagCompound = getNBTFromObject(inventoryCrafting);
            if (tagCompound != null) {
                stack.setTagCompound(tagCompound);
            }
        }
        return stack;
    }

}
