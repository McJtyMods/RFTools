package com.mcjty.rftools.crafting;

import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.items.ModItems;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class ModCrafting {

    public static final void init() {
        Object inkSac = Item.itemRegistry.getObjectById(351);
        GameRegistry.addRecipe(new ItemStack(ModItems.networkMonitorItem), new Object[]{"rlr", "iri", "rlr", 'r', Items.redstone, 'i', Items.iron_ingot, 'l', inkSac});

        ItemStack lapisStack = new ItemStack(Items.dye, 1, 4);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.machineFrame), new Object[]{"ili", "g g", "ili", 'i', Items.iron_ingot, 'g', Items.gold_nugget, 'l', lapisStack});

        Object redstoneTorch = Item.itemRegistry.getObject("redstone_torch");
        GameRegistry.addRecipe(new ItemStack(ModBlocks.monitorBlock), new Object[]{" T ", "rMr", " T ", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'r', Items.redstone});

        GameRegistry.addRecipe(new ItemStack(ModBlocks.crafterBlock1), new Object[]{" T ", "cMc", " T ", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'c', Blocks.crafting_table});
        GameRegistry.addRecipe(new ItemStack(ModBlocks.crafterBlock2), new Object[]{" T ", "cMc", " T ", 'M', ModBlocks.crafterBlock1, 'T', redstoneTorch, 'c', Blocks.crafting_table});
        GameRegistry.addRecipe(new ItemStack(ModBlocks.crafterBlock3), new Object[]{" T ", "cMc", " T ", 'M', ModBlocks.crafterBlock2, 'T', redstoneTorch, 'c', Blocks.crafting_table});

        GameRegistry.addRecipe(new ItemStack(ModBlocks.storageScannerBlock), new Object[]{"ToT", "gMg", "ToT", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'o', Items.ender_pearl,
                'g', Items.gold_ingot});

        GameRegistry.addRecipe(new ItemStack(ModBlocks.relayBlock), new Object[]{"gTg", "gMg", "gTg", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'g', Items.gold_ingot});

        GameRegistry.addRecipe(new ItemStack(ModBlocks.matterTransmitterBlock), new Object[]{"ooo", "rMr", "iii", 'M', ModBlocks.machineFrame,
                'o', Items.ender_pearl, 'r', Items.redstone, 'i', Items.iron_ingot});
        GameRegistry.addRecipe(new ItemStack(ModBlocks.matterReceiverBlock), new Object[]{"iii", "rMr", "ooo", 'M', ModBlocks.machineFrame,
                'o', Items.ender_pearl, 'r', Items.redstone, 'i', Items.iron_ingot});
        GameRegistry.addRecipe(new ItemStack(ModBlocks.dialingDeviceBlock), new Object[]{"rrr", "TMT", "rrr", 'M', ModBlocks.machineFrame, 'r', Items.redstone,
                'T', redstoneTorch});
        GameRegistry.addRecipe(new ItemStack(ModBlocks.destinationAnalyzerBlock), new Object[]{"o o", " M ", "o o", 'M', ModBlocks.machineFrame,
                'o', Items.ender_pearl});
    }
}
