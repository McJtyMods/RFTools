package mcjty.rftools.crafting;

import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class PreservingShapedOreRecipe extends ShapedOreRecipe {
    private ItemStack objectToInheritFrom;

    // @todo recipes
    public PreservingShapedOreRecipe(ResourceLocation group, Block result, ItemStack objectToInheritFrom, Object... recipe) {
        super(group, result, recipe);
        this.objectToInheritFrom = objectToInheritFrom;
    }

    //    public PreservingShapedOreRecipe(@Nonnull ItemStack result, int takeNBTFromSlot, Object... recipe) {
//        super(result, recipe);
//        objectToInheritFrom = (ItemStack) getInput()[takeNBTFromSlot];
//    }

    private NBTTagCompound getNBTFromObject(InventoryCrafting inventoryCrafting) {
        for (int i = 0 ; i < inventoryCrafting.getSizeInventory() ; i++) {
            ItemStack stack = inventoryCrafting.getStackInSlot(i);
            if (!stack.isEmpty() && stack.isItemEqual(objectToInheritFrom)) {
                return stack.getTagCompound();
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
