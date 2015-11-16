package mcjty.rftools.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;

public class ContainerAndItemRecipe extends ShapedRecipes {
    private Object objectToInheritFrom;
    private Object objectToGetEnergyFrom;

    public ContainerAndItemRecipe(ItemStack container, ItemStack item, ItemStack output) {
        super(2, 1, new ItemStack[] { container, item }, output);
        objectToInheritFrom = getObjectFromStack(item.getItem());
        objectToGetEnergyFrom = getObjectFromStack(container.getItem());
    }

    private Object getObjectFromStack(Item item) {
        if (item instanceof ItemBlock) {
            return ((ItemBlock) item).field_150939_a;
        } else {
            return item;
        }
    }

    private NBTTagCompound getNBTFromObject(InventoryCrafting inventoryCrafting, Object obj) {
        for (int i = 0 ; i < inventoryCrafting.getSizeInventory() ; i++) {
            ItemStack stack = inventoryCrafting.getStackInSlot(i);
            if (stack != null && stack.getItem() != null) {
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
            if (stack != null && stack.getItem() != null) {
                Object o = getObjectFromStack(stack.getItem());
                if (objectToInheritFrom.equals(o)) {
                    return stack.getItemDamage();
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
        if (stack != null) {
            NBTTagCompound tagCompound = getNBTFromObject(inventoryCrafting, objectToInheritFrom);
            if (tagCompound != null) {
                stack.setTagCompound(tagCompound);
            }
            NBTTagCompound tagCompoundEnergy = getNBTFromObject(inventoryCrafting, objectToGetEnergyFrom);
            if (tagCompoundEnergy != null) {
                getCompound(stack).setInteger("Energy", tagCompoundEnergy.getInteger("Energy"));
            }

            Integer damage = getDamageFromObject(inventoryCrafting);
            if (damage != null) {
                getCompound(stack).setInteger("childDamage", damage);
            }
        }
        return stack;
    }

}
