package mcjty.rftools.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

import java.util.function.Function;

public class ContainerAndItemRecipe extends ShapedRecipes {
    private Object objectToInheritFrom;
    private Object objectToGetEnergyFrom;
    private Function<InventoryCrafting,Integer> getMetaFunction;

    // @todo
    public ContainerAndItemRecipe(String group, int width, int height, NonNullList<Ingredient> ingredients, ItemStack result, Object objectToInheritFrom) {
        super(group, width, height, ingredients, result);
        this.objectToInheritFrom = objectToInheritFrom;
    }

    //    public ContainerAndItemRecipe(ItemStack container, ItemStack item, ItemStack output,
//                                  Function<InventoryCrafting,Integer> getMetaFunction) {
//        super(2, 1, new ItemStack[] { container, item }, output);
//        objectToInheritFrom = getObjectFromStack(item.getItem());
//        objectToGetEnergyFrom = getObjectFromStack(container.getItem());
//        this.getMetaFunction = getMetaFunction == null ? inventoryCrafting -> getDamageFromObject(inventoryCrafting) : getMetaFunction;
//    }

    private Object getObjectFromStack(Item item) {
        if (item instanceof ItemBlock) {
            return ((ItemBlock) item).getBlock();
        } else {
            return item;
        }
    }

    private NBTTagCompound getNBTFromObject(InventoryCrafting inventoryCrafting, Object obj) {
        for (int i = 0 ; i < inventoryCrafting.getSizeInventory() ; i++) {
            ItemStack stack = inventoryCrafting.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() != null) {
                Object o = getObjectFromStack(stack.getItem());
                if (obj.equals(o)) {
                    return stack.getTagCompound();
                }
            }
        }
        return null;
    }

    private Integer getDamageFromObject(InventoryCrafting inventoryCrafting) {
        for (int i = 0 ; i < inventoryCrafting.getSizeInventory() ; i++) {
            ItemStack stack = inventoryCrafting.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() != null) {
                Object o = getObjectFromStack(stack.getItem());
                if (objectToInheritFrom.equals(o)) {
                    return stack.getMetadata();
                }
            }
        }
        return null;
    }

    private static NBTTagCompound getCompound(ItemStack stack) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }
        return stack.getTagCompound();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {
        ItemStack stack = super.getCraftingResult(inventoryCrafting);
        if (!stack.isEmpty()) {
            NBTTagCompound tagCompound = getNBTFromObject(inventoryCrafting, objectToInheritFrom);
            if (tagCompound != null) {
                stack.setTagCompound(tagCompound);
            }
            NBTTagCompound tagCompoundTablet = getNBTFromObject(inventoryCrafting, objectToGetEnergyFrom);
            if (tagCompoundTablet != null) {
                getCompound(stack).setInteger("Energy", tagCompoundTablet.getInteger("Energy"));
                // @todo hardcoded!
                if (tagCompoundTablet.hasKey("grid")) {
                    getCompound(stack).setTag("grid", tagCompoundTablet.getTag("grid"));
                }
            }

            Integer damage = getMetaFunction.apply(inventoryCrafting);
            if (damage != null) {
                getCompound(stack).setInteger("childDamage", damage);
            }
        }
        return stack;
    }

}
