package com.mcjty.rftools.crafting;

import com.mcjty.rftools.items.ModItems;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletType;
import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.HashMap;

public class KnownDimletShapedRecipe extends ShapedRecipes {
    protected DimletKey dimletKey;
    protected DimletKey sourceDimletKey = null;

    public KnownDimletShapedRecipe(DimletKey dimletKey, Object... items) {
        this(dimletKey, false, createItemStack(items));
    }

    // The dummy is to be able to call this constructor instead of the above one.
    public KnownDimletShapedRecipe(DimletKey dimletKey, boolean dummy, ItemStack[] items) {
        super(3, 3, items, new ItemStack(ModItems.knownDimlet));
        this.dimletKey = dimletKey;
        this.sourceDimletKey = null;
    }

    public KnownDimletShapedRecipe(String source, String dest) {
        this(new DimletKey(DimletType.DIMLET_DIGIT, dest), false, createSourceItemStack());
        sourceDimletKey = new DimletKey(DimletType.DIMLET_DIGIT, source);
    }

    private static ItemStack[] createSourceItemStack() {
        ItemStack[] stacks = new ItemStack[9];
        stacks[4] = new ItemStack(ModItems.knownDimlet, 1, 0);
        return stacks;
    }

    private static ItemStack[] createItemStack(Object... items) {
        String s = "";
        int i = 0;
        int j = 0;
        int k = 0;

        if (items[i] instanceof String[]) {
            String[] astring = (String[])items[i++];

            for (String s1 : astring) {
                ++k;
                j = s1.length();
                s = s + s1;
            }
        } else {
            while (items[i] instanceof String) {
                String s2 = (String)items[i++];
                ++k;
                j = s2.length();
                s = s + s2;
            }
        }

        HashMap hashmap;

        for (hashmap = new HashMap(); i < items.length; i += 2) {
            Character character = (Character)items[i];
            ItemStack itemstack1 = null;

            if (items[i + 1] instanceof Item) {
                itemstack1 = new ItemStack((Item)items[i + 1]);
            } else if (items[i + 1] instanceof Block) {
                itemstack1 = new ItemStack((Block)items[i + 1], 1, 32767);
            } else if (items[i + 1] instanceof ItemStack) {
                itemstack1 = (ItemStack)items[i + 1];
            }

            hashmap.put(character, itemstack1);
        }

        ItemStack[] aitemstack = new ItemStack[j * k];

        for (int i1 = 0; i1 < j * k; ++i1) {
            char c0 = s.charAt(i1);

            if (hashmap.containsKey(Character.valueOf(c0))) {
                aitemstack[i1] = ((ItemStack)hashmap.get(Character.valueOf(c0))).copy();
            } else {
                aitemstack[i1] = null;
            }
        }
        return aitemstack;
    }


    @Override
    public ItemStack getRecipeOutput() {
        ItemStack stack = super.getRecipeOutput().copy();
        Integer id = KnownDimletConfiguration.dimletToID.get(dimletKey);
        if (id != null) {
            stack.setItemDamage(id);
        }
        if (sourceDimletKey != null) {
            id = KnownDimletConfiguration.dimletToID.get(sourceDimletKey);
            if (id != null) {
                recipeItems[4].setItemDamage(id);
            }
        }
        return stack;
    }

    @Override
    public boolean matches(InventoryCrafting inventoryCrafting, World world) {
        return super.matches(inventoryCrafting, world);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {
        ItemStack stack = super.getCraftingResult(inventoryCrafting);
        Integer id = KnownDimletConfiguration.dimletToID.get(dimletKey);
        if (id != null) {
            stack.setItemDamage(id);
        }
        if (sourceDimletKey != null) {
            id = KnownDimletConfiguration.dimletToID.get(sourceDimletKey);
            if (id != null) {
                recipeItems[4].setItemDamage(id);
            }
        }
        return stack;
    }
}
