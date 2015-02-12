package com.mcjty.rftools.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

public class PreservingShapedRecipe extends ShapedRecipes {
    private int takeNBTFromSlot;
    private Map<String,Object> extraNBT = null;

    public PreservingShapedRecipe(int width, int height, ItemStack[] items, ItemStack output, int takeNBTFromSlot) {
        super(width, height, items, output);
        this.takeNBTFromSlot = takeNBTFromSlot;
    }


    public PreservingShapedRecipe(int width, int height, ItemStack[] items, ItemStack output, int takeNBTFromSlot, Map<String,Object> extraNBT) {
        this(width, height, items, output, takeNBTFromSlot);
        this.extraNBT = new HashMap<String, Object>(extraNBT);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {
        ItemStack stack = super.getCraftingResult(inventoryCrafting);
        if (stack != null) {
            NBTTagCompound tagCompound = inventoryCrafting.getStackInSlot(takeNBTFromSlot).getTagCompound();
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
