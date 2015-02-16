package com.mcjty.rftools.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

public class PreservingShapedRecipe extends ShapedRecipes {
    private Object objectToInheritFrom;
    private Map<String,Object> extraNBT = null;

    public PreservingShapedRecipe(int width, int height, ItemStack[] items, ItemStack output, int takeNBTFromSlot) {
        super(width, height, items, output);
        Item item = items[takeNBTFromSlot].getItem();
        objectToInheritFrom = getObjectFromStack(item);
    }

    private Object getObjectFromStack(Item item) {
        if (item instanceof ItemBlock) {
            return ((ItemBlock) item).field_150939_a;
        } else {
            return item;
        }
    }


    public PreservingShapedRecipe(int width, int height, ItemStack[] items, ItemStack output, int takeNBTFromSlot, Map<String,Object> extraNBT) {
        this(width, height, items, output, takeNBTFromSlot);
        this.extraNBT = new HashMap<String, Object>(extraNBT);
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
            if (extraNBT != null) {
                if (tagCompound == null) {
                    tagCompound = new NBTTagCompound();
                } else {
                    tagCompound = (NBTTagCompound) tagCompound.copy();
                }
                for (Map.Entry<String, Object> entry : extraNBT.entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof Integer) {
                        tagCompound.setInteger(entry.getKey(), (Integer) value);
                    } else if (value instanceof Boolean) {
                        tagCompound.setBoolean(entry.getKey(), (Boolean) value);
                    } else if (value instanceof String) {
                        tagCompound.setString(entry.getKey(), (String) value);
                    } else if (value instanceof Double) {
                        tagCompound.setDouble(entry.getKey(), (Double) value);
                    } else if (value instanceof Float) {
                        tagCompound.setFloat(entry.getKey(), (Float) value);
                    } else {
                        throw new RuntimeException("Unknown extra NBT tag type");
                    }
                }
            }
            if (tagCompound != null) {
                stack.setTagCompound(tagCompound);
            }
        }
        return stack;
    }

}
