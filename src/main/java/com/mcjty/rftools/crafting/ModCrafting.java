package com.mcjty.rftools.crafting;

import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.items.ModItems;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class ModCrafting {

    public static final void init() {
        Object inkSac = Item.itemRegistry.getObjectById(351);
        GameRegistry.addRecipe(new ItemStack(ModItems.networkMonitorItem), new Object[]{"rlr", "iri", "rlr", 'r', Items.redstone, 'i', Items.iron_ingot, 'l', inkSac});

        Object redstoneTorch = Item.itemRegistry.getObjectById(76);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.monitorBlock), new Object[]{"iTi", "iri", "iTi", 'i', Items.iron_ingot, 'T', redstoneTorch, 'r', Items.redstone});
    }
}
