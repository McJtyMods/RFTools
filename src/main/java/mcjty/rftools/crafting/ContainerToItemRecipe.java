package mcjty.rftools.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;

public class ContainerToItemRecipe extends ShapedRecipes {
    private Object objectToInheritFrom;

    public ContainerToItemRecipe(ItemStack item, ItemStack output) {
        super(1, 1, new ItemStack[] { item }, output);
        objectToInheritFrom = getObjectFromStack(item.getItem());
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
            if (stack != null && stack.getItem() != null) {
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
        if (stack != null) {
            NBTTagCompound tagCompound = getNBTFromObject(inventoryCrafting);
            if (tagCompound != null) {
                int damage = tagCompound.getInteger("childDamage");
                NBTTagCompound newtag = new NBTTagCompound();
                for (Object o : tagCompound.getKeySet()) {
                    String tag = (String) o;
                    if ((!"childDamage".equals(tag)) && (!"Energy".equals(tag))) {
                        newtag.setTag(tag, tagCompound.getTag(tag));
                    }
                }

                stack.setTagCompound(newtag);
                stack.setItemDamage(damage);
            }
        }
        return stack;
    }

}
