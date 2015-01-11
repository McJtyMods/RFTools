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
        GameRegistry.addRecipe(new ItemStack(ModItems.networkMonitorItem), "rlr", "iri", "rlr", 'r', Items.redstone, 'i', Items.iron_ingot, 'l', inkSac);

        GameRegistry.addRecipe(new ItemStack(ModItems.rfToolsManualItem), " r ", "rbr", " r ", 'r', Items.redstone, 'b', Items.book);
        GameRegistry.addRecipe(new ItemStack(ModItems.rfToolsManualDimensionItem), "r r", " b ", "r r", 'r', Items.redstone, 'b', Items.book, 'u', ModItems.unknownDimlet);

        ItemStack lapisStack = new ItemStack(Items.dye, 1, 4);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.machineFrame), "ili", "g g", "ili", 'i', Items.iron_ingot, 'g', Items.gold_nugget, 'l', lapisStack);

        GameRegistry.addRecipe(new ItemStack(ModBlocks.machineBase), "   ", "ggg", "sss", 'g', Items.gold_nugget, 's', Blocks.stone);

        Object redstoneTorch = Item.itemRegistry.getObject("redstone_torch");
        GameRegistry.addRecipe(new ItemStack(ModBlocks.monitorBlock), " T ", "rMr", " T ", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'r', Items.redstone);

        GameRegistry.addRecipe(new ItemStack(ModBlocks.crafterBlock1), " T ", "cMc", " T ", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'c', Blocks.crafting_table);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.crafterBlock2), " T ", "cMc", " T ", 'M', ModBlocks.crafterBlock1, 'T', redstoneTorch, 'c', Blocks.crafting_table);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.crafterBlock3), " T ", "cMc", " T ", 'M', ModBlocks.crafterBlock2, 'T', redstoneTorch, 'c', Blocks.crafting_table);

        GameRegistry.addRecipe(new ItemStack(ModBlocks.machineInfuserBlock), "srs", "dMd", "srs", 'M', ModBlocks.machineFrame, 's', ModItems.dimensionalShard,
                'r', Items.redstone, 'd', Items.diamond);

        GameRegistry.addRecipe(new ItemStack(ModBlocks.storageScannerBlock), "ToT", "gMg", "ToT", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'o', Items.ender_pearl,
                'g', Items.gold_ingot);

        GameRegistry.addRecipe(new ItemStack(ModBlocks.relayBlock), "gTg", "gMg", "gTg", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'g', Items.gold_ingot);

        GameRegistry.addRecipe(new ItemStack(ModBlocks.itemFilterBlock), "pcp", "rMr", "pTp", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'p', Items.paper,
                'r', Items.redstone, 'c', Blocks.chest);

        GameRegistry.addRecipe(new ItemStack(ModBlocks.matterTransmitterBlock), "ooo", "rMr", "iii", 'M', ModBlocks.machineFrame,
                'o', Items.ender_pearl, 'r', Items.redstone, 'i', Items.iron_ingot);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.matterReceiverBlock), "iii", "rMr", "ooo", 'M', ModBlocks.machineFrame,
                'o', Items.ender_pearl, 'r', Items.redstone, 'i', Items.iron_ingot);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.dialingDeviceBlock), "rrr", "TMT", "rrr", 'M', ModBlocks.machineFrame, 'r', Items.redstone,
                'T', redstoneTorch);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.destinationAnalyzerBlock), "o o", " M ", "o o", 'M', ModBlocks.machineFrame,
                'o', Items.ender_pearl);

        GameRegistry.addRecipe(new ItemStack(ModBlocks.sequencerBlock), "rTr", "TMT", "rTr", 'r', Items.redstone, 'T', redstoneTorch, 'M', ModBlocks.machineBase);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.timerBlock), "rcr", "TMT", "rTr", 'c', Items.clock, 'r', Items.redstone, 'T', redstoneTorch, 'M', ModBlocks.machineBase);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.enderMonitorBlock), "ror", "TMT", "rTr", 'o', Items.ender_pearl, 'r', Items.redstone, 'T', redstoneTorch, 'M', ModBlocks.machineBase);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.dimensionMonitorBlock), " u ", "TMT", "rCr", 'u', ModItems.unknownDimlet, 'r', Items.redstone, 'T', redstoneTorch, 'M', ModBlocks.machineBase,
                'C', Items.comparator);

        GameRegistry.addRecipe(new ItemStack(ModBlocks.endergenicBlock), "DoD", "oMo", "DoD", 'M', ModBlocks.machineFrame, 'D', Items.diamond, 'o', Items.ender_pearl);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.pearlInjectorBlock), " C ", "rMr", " H ", 'C', Blocks.chest, 'r', Items.redstone,
                'M', ModBlocks.machineFrame, 'H', Blocks.hopper);

        GameRegistry.addRecipe(new ItemStack(ModBlocks.shieldBlock), "gTg", "rMr", "ooo", 'M', ModBlocks.machineFrame, 'o', Blocks.obsidian,
                'r', Items.redstone, 'T', redstoneTorch, 'g', Items.gold_ingot);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.shieldTemplateBlock, 8), "www", "lgl", "www", 'w', Blocks.wool, 'l', lapisStack, 'g', Blocks.glass);

        GameRegistry.addSmelting(ModBlocks.dimensionalShardBlock, new ItemStack(ModItems.dimensionalShard, 4), 1.0f);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.dimletResearcherBlock), "rur", "cMc", "iii", 'r', Items.redstone, 'u', ModItems.unknownDimlet, 'c', Items.comparator,
                'M', ModBlocks.machineFrame, 'i', Items.iron_ingot);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.dimletScramblerBlock), "uru", "cMc", "iii", 'r', Items.redstone, 'u', ModItems.unknownDimlet, 'c', Items.repeater,
                'M', ModBlocks.machineFrame, 'i', Items.iron_ingot);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.dimensionEnscriberBlock), "rpr", "bMb", "iii", 'r', Items.redstone, 'p', Items.paper, 'b', inkSac,
                'M', ModBlocks.machineFrame, 'i', Items.iron_ingot);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.dimensionBuilderBlock), "oEo", "DMD", "ggg", 'o', Items.ender_pearl, 'E', Items.emerald, 'D', Items.diamond,
                'M', ModBlocks.machineFrame, 'g', Items.gold_ingot);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.dimensionEditorBlock), "oEo", "DMD", "ggg", 'o', Items.redstone, 'E', Items.emerald, 'D', Items.diamond,
                'M', ModBlocks.machineFrame, 'g', Items.gold_ingot);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.activityProbeBlock), "sss", "oMo", "sss", 'o', Items.ender_pearl, 's', ModItems.dimensionalShard,
                'M', ModBlocks.machineFrame);

        GameRegistry.addRecipe(new ItemStack(ModItems.emptyDimensionTab), "prp", "rpr", "prp", 'p', Items.paper, 'r', Items.redstone);
        GameRegistry.addRecipe(new ItemStack(ModItems.dimensionMonitorItem), " u ", "rCr", " u ", 'u', ModItems.unknownDimlet, 'r', Items.redstone, 'C', Items.comparator);

        GameRegistry.addRecipe(new ItemStack(ModBlocks.dimensionalBlankBlock, 8), "bbb", "b*b", "bbb", 'b', Blocks.stone, '*', ModItems.dimensionalShard);
        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.dimensionalBlock), new ItemStack(ModBlocks.dimensionalBlankBlock));
        GameRegistry.addRecipe(new ItemStack(ModBlocks.dimensionalSmallBlocks, 4), "bb ", "bb ", "   ", 'b', ModBlocks.dimensionalBlankBlock);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.dimensionalCrossBlock, 5), " b ", "bbb", " b ", 'b', ModBlocks.dimensionalBlankBlock);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.dimensionalCross2Block, 5), "b b", " b ", "b b", 'b', ModBlocks.dimensionalBlankBlock);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.dimensionalPattern1Block, 7), "bxb", "bbb", "bxb", 'b', ModBlocks.dimensionalBlankBlock, 'x', inkSac);
        ItemStack bonemealStack = new ItemStack(Items.dye, 1, 15);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.dimensionalPattern2Block, 7), "bxb", "bbb", "bxb", 'b', ModBlocks.dimensionalBlankBlock, 'x', bonemealStack);

        // Recipes for known dimlets are added in KnownDimletConfiguration.
    }
}
