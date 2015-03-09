package com.mcjty.rftools.crafting;

import com.mcjty.rftools.items.ModItems;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class KnownDimletShapedRecipe extends ShapedOreRecipe {
    private DimletKey destDimletKey;

    public KnownDimletShapedRecipe(DimletKey destDimletKey, Object... items) {
        super(new ItemStack(ModItems.knownDimlet), items);
        this.destDimletKey = destDimletKey;
    }

    @Override
    public ItemStack getRecipeOutput() {
        ItemStack stack = super.getRecipeOutput().copy();
        KnownDimletConfiguration.setDimletKey(destDimletKey, stack);
        return stack;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {
        ItemStack stack = super.getCraftingResult(inventoryCrafting);
        KnownDimletConfiguration.setDimletKey(destDimletKey, stack);
        return stack;
    }
}
