package com.mcjty.rftools.crafting;

import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.items.ModItems;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletType;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.RecipeSorter;

public final class ModCrafting {
    static {
        RecipeSorter.register("rftools:shapedpreserving", PreservingShapedRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register("rftools:shapedknowndimlet", KnownDimletShapedRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
    }

    public static void init() {
        Object inkSac = Item.itemRegistry.getObjectById(351);
        GameRegistry.addRecipe(new ItemStack(ModItems.networkMonitorItem), "rlr", "iri", "rlr", 'r', Items.redstone, 'i', Items.iron_ingot, 'l', inkSac);

        GameRegistry.addRecipe(new ItemStack(ModItems.rfToolsManualItem), " r ", "rbr", " r ", 'r', Items.redstone, 'b', Items.book);
        GameRegistry.addRecipe(new ItemStack(ModItems.rfToolsManualDimensionItem), "r r", " b ", "r r", 'r', Items.redstone, 'b', Items.book);

        ItemStack lapisStack = new ItemStack(Items.dye, 1, 4);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.machineFrame), "ili", "g g", "ili", 'i', Items.iron_ingot, 'g', Items.gold_nugget, 'l', lapisStack);

        GameRegistry.addRecipe(new ItemStack(ModBlocks.machineBase), "   ", "ggg", "sss", 'g', Items.gold_nugget, 's', Blocks.stone);

        Object redstoneTorch = Item.itemRegistry.getObject("redstone_torch");
        GameRegistry.addRecipe(new ItemStack(ModBlocks.monitorBlock), " T ", "rMr", " T ", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'r', Items.redstone);

        GameRegistry.addRecipe(new ItemStack(ModBlocks.crafterBlock1), " T ", "cMc", " T ", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'c', Blocks.crafting_table);
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                null, new ItemStack((Item) redstoneTorch), null,
                new ItemStack(Blocks.crafting_table), new ItemStack(ModBlocks.crafterBlock1), new ItemStack(Blocks.crafting_table),
                null, new ItemStack((Item) redstoneTorch), null
        }, new ItemStack(ModBlocks.crafterBlock2), 4));
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                null, new ItemStack((Item) redstoneTorch), null,
                new ItemStack(Blocks.crafting_table), new ItemStack(ModBlocks.crafterBlock2), new ItemStack(Blocks.crafting_table),
                null, new ItemStack((Item) redstoneTorch), null
        }, new ItemStack(ModBlocks.crafterBlock3), 4));

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

        GameRegistry.addRecipe(new ItemStack(ModBlocks.screenControllerBlock), "ror", "gMg", "rgr", 'r', Items.redstone, 'o', Items.ender_pearl, 'M', ModBlocks.machineFrame,
                'g', Blocks.glass);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.screenBlock), "ggg", "gMg", "iii", 'M', ModBlocks.machineBase,
                'g', Blocks.glass, 'i', Items.iron_ingot);
        GameRegistry.addRecipe(new ItemStack(ModItems.textModuleItem), " p ", "rir", " b ", 'p', Items.paper, 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);
        GameRegistry.addRecipe(new ItemStack(ModItems.clockModuleItem), " c ", "rir", " b ", 'c', Items.clock, 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);
        GameRegistry.addRecipe(new ItemStack(ModItems.energyModuleItem), " r ", "rir", " b ", 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);
        GameRegistry.addRecipe(new ItemStack(ModItems.dimensionModuleItem), " c ", "rir", " b ", 'c', Items.ender_pearl, 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);
        GameRegistry.addRecipe(new ItemStack(ModItems.fluidModuleItem), " c ", "rir", " b ", 'c', Items.bucket, 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);

        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                null, new ItemStack(Items.ender_pearl), null,
                new ItemStack(Items.gold_ingot), new ItemStack(ModItems.energyModuleItem), new ItemStack(Items.gold_ingot),
                null, new ItemStack(Items.ender_pearl), null }, new ItemStack(ModItems.energyPlusModuleItem), 4));
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                null, new ItemStack(Items.ender_pearl), null,
                new ItemStack(Items.gold_ingot), new ItemStack(ModItems.fluidModuleItem), new ItemStack(Items.gold_ingot),
                null, new ItemStack(Items.ender_pearl), null }, new ItemStack(ModItems.fluidPlusModuleItem), 4));

        GameRegistry.addRecipe(new ItemStack(ModItems.dimletTemplate), "sss", "sps", "sss", 's', ModItems.dimensionalShard, 'p', Items.paper);

        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_EFFECT, "None"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.apple, 'p', Items.paper));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_FEATURE, "None"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.string, 'p', Items.paper));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_STRUCTURE, "None"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.bone, 'p', Items.paper));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_TERRAIN, "Void"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.brick, 'p', Items.paper));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_TERRAIN, "Flat"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.brick, 'p', ModItems.dimletTemplate));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_CONTROLLER, "Default"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.comparator, 'p', Items.paper));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_CONTROLLER, "Single"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.comparator, 'p', ModItems.dimletTemplate));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_MATERIAL, "None"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Blocks.dirt, 'p', Items.paper));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_LIQUID, "None"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.bucket, 'p', Items.paper));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_SKY, "Normal"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.feather, 'p', ModItems.dimletTemplate));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_SKY, "Normal Day"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.glowstone_dust, 'p', ModItems.dimletTemplate));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_SKY, "Normal Night"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.coal, 'p', Items.paper));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_MOBS, "Default"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.rotten_flesh, 'p', ModItems.dimletTemplate));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_TIME, "Normal"), " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.clock, 'p', ModItems.dimletTemplate));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe(new DimletKey(DimletType.DIMLET_DIGIT, "0"), " r ", "rtr", "ppp", 'r', Items.redstone, 't', redstoneTorch, 'p', Items.paper));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe("0", "1"));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe("1", "2"));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe("2", "3"));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe("3", "4"));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe("4", "5"));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe("5", "6"));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe("6", "7"));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe("7", "8"));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe("8", "9"));
        GameRegistry.addRecipe(new KnownDimletShapedRecipe("9", "0"));
    }

}
