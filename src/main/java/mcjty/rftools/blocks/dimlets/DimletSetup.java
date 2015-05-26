package mcjty.rftools.blocks.dimlets;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.container.GenericItemBlock;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.shards.*;
import mcjty.rftools.items.dimensionmonitor.DimensionMonitorItem;
import mcjty.rftools.items.dimensionmonitor.PhasedFieldGeneratorItem;
import mcjty.rftools.items.dimlets.*;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class DimletSetup {
    public static DimletResearcherBlock dimletResearcherBlock;
    public static DimletScramblerBlock dimletScramblerBlock;
    public static DimensionEnscriberBlock dimensionEnscriberBlock;
    public static DimensionBuilderBlock dimensionBuilderBlock;
    public static DimensionBuilderBlock creativeDimensionBuilderBlock;
    public static DimensionEditorBlock dimensionEditorBlock;
    public static DimensionMonitorBlock dimensionMonitorBlock;
    public static DimensionalShardBlock dimensionalShardBlock;
    public static DimensionalBlankBlock dimensionalBlankBlock;
    public static DimensionalBlock dimensionalBlock;
    public static DimensionalSmallBlocks dimensionalSmallBlocks;
    public static DimensionalCrossBlock dimensionalCrossBlock;
    public static DimensionalCross2Block dimensionalCross2Block;
    public static DimensionalPattern1Block dimensionalPattern1Block;
    public static DimensionalPattern2Block dimensionalPattern2Block;
    public static ActivityProbeBlock activityProbeBlock;
    public static EnergyExtractorBlock energyExtractorBlock;

    public static UnknownDimlet unknownDimlet;
    public static DimletTemplate dimletTemplate;
    public static KnownDimlet knownDimlet;
    public static EmptyDimensionTab emptyDimensionTab;
    public static RealizedDimensionTab realizedDimensionTab;
    public static DimensionMonitorItem dimensionMonitorItem;
    public static PhasedFieldGeneratorItem phasedFieldGeneratorItem;
    public static DimensionalShard dimensionalShard;

    public static void setupBlocks() {
        dimletResearcherBlock = new DimletResearcherBlock();
        GameRegistry.registerBlock(dimletResearcherBlock, GenericItemBlock.class, "dimletResearcherBlock");
        GameRegistry.registerTileEntity(DimletResearcherTileEntity.class, "DimletResearcherTileEntity");

        dimletScramblerBlock = new DimletScramblerBlock();
        GameRegistry.registerBlock(dimletScramblerBlock, GenericItemBlock.class, "dimletScramblerBlock");
        GameRegistry.registerTileEntity(DimletScramblerTileEntity.class, "DimletScramblerTileEntity");

        dimensionEnscriberBlock = new DimensionEnscriberBlock();
        GameRegistry.registerBlock(dimensionEnscriberBlock, GenericItemBlock.class, "dimensionEnscriberBlock");
        GameRegistry.registerTileEntity(DimensionEnscriberTileEntity.class, "DimensionEnscriberTileEntity");

        dimensionBuilderBlock = new DimensionBuilderBlock(false, "dimensionBuilderBlock");
        GameRegistry.registerBlock(dimensionBuilderBlock, GenericItemBlock.class, "dimensionBuilderBlock");
        GameRegistry.registerTileEntity(DimensionBuilderTileEntity.class, "DimensionBuilderTileEntity");

        creativeDimensionBuilderBlock = new DimensionBuilderBlock(true, "creativeDimensionBuilderBlock");
        GameRegistry.registerBlock(creativeDimensionBuilderBlock, GenericItemBlock.class, "creativeDimensionBuilderBlock");

        dimensionEditorBlock = new DimensionEditorBlock();
        GameRegistry.registerBlock(dimensionEditorBlock, GenericItemBlock.class, "dimensionEditorBlock");
        GameRegistry.registerTileEntity(DimensionEditorTileEntity.class, "DimensionEditorTileEntity");

        dimensionMonitorBlock = new DimensionMonitorBlock();
        GameRegistry.registerBlock(dimensionMonitorBlock, GenericItemBlock.class, "dimensionMonitorBlock");
        GameRegistry.registerTileEntity(DimensionMonitorTileEntity.class, "DimensionMonitorTileEntity");

        dimensionalShardBlock = new DimensionalShardBlock();
        GameRegistry.registerBlock(dimensionalShardBlock, "dimensionalShardBlock");
        dimensionalBlankBlock = new DimensionalBlankBlock();
        GameRegistry.registerBlock(dimensionalBlankBlock, "dimensionalBlankBlock");
        dimensionalBlock = new DimensionalBlock();
        GameRegistry.registerBlock(dimensionalBlock, "dimensionalBlock");
        dimensionalSmallBlocks = new DimensionalSmallBlocks();
        GameRegistry.registerBlock(dimensionalSmallBlocks, "dimensionalSmallBlocks");
        dimensionalCrossBlock = new DimensionalCrossBlock();
        GameRegistry.registerBlock(dimensionalCrossBlock, "dimensionalCrossBlock");
        dimensionalCross2Block = new DimensionalCross2Block();
        GameRegistry.registerBlock(dimensionalCross2Block, "dimensionalCross2Block");
        dimensionalPattern1Block = new DimensionalPattern1Block();
        GameRegistry.registerBlock(dimensionalPattern1Block, "dimensionalPattern1Block");
        dimensionalPattern2Block = new DimensionalPattern2Block();
        GameRegistry.registerBlock(dimensionalPattern2Block, "dimensionalPattern2Block");

        activityProbeBlock = new ActivityProbeBlock();
        GameRegistry.registerBlock(activityProbeBlock, "activityProbeBlock");

        energyExtractorBlock = new EnergyExtractorBlock();
        GameRegistry.registerBlock(energyExtractorBlock, GenericItemBlock.class, "energyExtractorBlock");
        GameRegistry.registerTileEntity(EnergyExtractorTileEntity.class, "EnergyExtractorTileEntity");
    }

    public static void setupItems() {
        unknownDimlet = new UnknownDimlet();
        unknownDimlet.setUnlocalizedName("UnknownDimlet");
        unknownDimlet.setCreativeTab(RFTools.tabRfToolsDimlets);
        unknownDimlet.setTextureName(RFTools.MODID + ":unknownDimletItem");
        GameRegistry.registerItem(unknownDimlet, "unknownDimlet");

        knownDimlet = new KnownDimlet();
        knownDimlet.setUnlocalizedName("KnownDimlet");
        knownDimlet.setCreativeTab(RFTools.tabRfToolsDimlets);
        GameRegistry.registerItem(knownDimlet, "knownDimlet");
        OreDictionary.registerOre("dimlet", knownDimlet);

        dimletTemplate = new DimletTemplate();
        dimletTemplate.setUnlocalizedName("DimletTemplate");
        dimletTemplate.setCreativeTab(RFTools.tabRfToolsDimlets);
        dimletTemplate.setTextureName(RFTools.MODID + ":dimletTemplateItem");
        GameRegistry.registerItem(dimletTemplate, "dimletTemplate");

        emptyDimensionTab = new EmptyDimensionTab();
        emptyDimensionTab.setUnlocalizedName("EmptyDimensionTab");
        emptyDimensionTab.setCreativeTab(RFTools.tabRfTools);
        emptyDimensionTab.setTextureName(RFTools.MODID + ":emptyDimensionTabItem");
        GameRegistry.registerItem(emptyDimensionTab, "emptyDimensionTab");

        realizedDimensionTab = new RealizedDimensionTab();
        realizedDimensionTab.setUnlocalizedName("RealizedDimensionTab");
        realizedDimensionTab.setCreativeTab(RFTools.tabRfTools);
        realizedDimensionTab.setTextureName(RFTools.MODID + ":realizedDimensionTabItem");
        GameRegistry.registerItem(realizedDimensionTab, "realizedDimensionTab");

        dimensionMonitorItem = new DimensionMonitorItem();
        dimensionMonitorItem.setUnlocalizedName("DimensionMonitor");
        dimensionMonitorItem.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerItem(dimensionMonitorItem, "dimensionMonitorItem");

        phasedFieldGeneratorItem = new PhasedFieldGeneratorItem();
        phasedFieldGeneratorItem.setUnlocalizedName("PhasedFieldGenerator");
        phasedFieldGeneratorItem.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerItem(phasedFieldGeneratorItem, "phasedFieldGeneratorItem");

        dimensionalShard = new DimensionalShard();
        dimensionalShard.setUnlocalizedName("DimensionalShard");
        dimensionalShard.setCreativeTab(RFTools.tabRfTools);
        dimensionalShard.setTextureName(RFTools.MODID + ":dimensionalShardItem");
        GameRegistry.registerItem(dimensionalShard, "dimensionalShardItem");
    }

    public static void setupCrafting() {
        Object inkSac = Item.itemRegistry.getObjectById(351);

        GameRegistry.addSmelting(dimensionalShardBlock, new ItemStack(dimensionalShard, 4), 1.0f);
        GameRegistry.addRecipe(new ItemStack(dimletResearcherBlock), "rur", "cMc", "iii", 'r', Items.redstone, 'u', unknownDimlet, 'c', Items.comparator,
                'M', ModBlocks.machineFrame, 'i', Items.iron_ingot);
        GameRegistry.addRecipe(new ItemStack(dimletScramblerBlock), "uru", "cMc", "iii", 'r', Items.redstone, 'u', unknownDimlet, 'c', Items.repeater,
                'M', ModBlocks.machineFrame, 'i', Items.iron_ingot);
        GameRegistry.addRecipe(new ItemStack(dimensionEnscriberBlock), "rpr", "bMb", "iii", 'r', Items.redstone, 'p', Items.paper, 'b', inkSac,
                'M', ModBlocks.machineFrame, 'i', Items.iron_ingot);
        GameRegistry.addRecipe(new ItemStack(dimensionBuilderBlock), "oEo", "DMD", "ggg", 'o', Items.ender_pearl, 'E', Items.emerald, 'D', Items.diamond,
                'M', ModBlocks.machineFrame, 'g', Items.gold_ingot);
        GameRegistry.addRecipe(new ItemStack(dimensionEditorBlock), "oEo", "DMD", "ggg", 'o', Items.redstone, 'E', Items.emerald, 'D', Items.diamond,
                'M', ModBlocks.machineFrame, 'g', Items.gold_ingot);
        GameRegistry.addRecipe(new ItemStack(activityProbeBlock), "sss", "oMo", "sss", 'o', Items.ender_pearl, 's', dimensionalShard,
                'M', ModBlocks.machineFrame);
        GameRegistry.addRecipe(new ItemStack(energyExtractorBlock), "RoR", "sMs", "RsR", 'o', Items.ender_pearl, 's', dimensionalShard,
                'M', ModBlocks.machineFrame, 'R', Blocks.redstone_block);

        GameRegistry.addRecipe(new ItemStack(dimensionalBlankBlock, 8), "bbb", "b*b", "bbb", 'b', Blocks.stone, '*', dimensionalShard);
        GameRegistry.addShapelessRecipe(new ItemStack(dimensionalBlock), new ItemStack(dimensionalBlankBlock));
        GameRegistry.addRecipe(new ItemStack(dimensionalSmallBlocks, 4), "bb ", "bb ", "   ", 'b', dimensionalBlankBlock);
        GameRegistry.addRecipe(new ItemStack(dimensionalCrossBlock, 5), " b ", "bbb", " b ", 'b', dimensionalBlankBlock);
        GameRegistry.addRecipe(new ItemStack(dimensionalCross2Block, 5), "b b", " b ", "b b", 'b', dimensionalBlankBlock);
        GameRegistry.addRecipe(new ItemStack(dimensionalPattern1Block, 7), "bxb", "bbb", "bxb", 'b', dimensionalBlankBlock, 'x', inkSac);
        ItemStack bonemealStack = new ItemStack(Items.dye, 1, 15);
        GameRegistry.addRecipe(new ItemStack(dimensionalPattern2Block, 7), "bxb", "bbb", "bxb", 'b', dimensionalBlankBlock, 'x', bonemealStack);

        Object redstoneTorch = Item.itemRegistry.getObject("redstone_torch");
        GameRegistry.addRecipe(new ItemStack(DimletSetup.dimensionMonitorBlock), " u ", "TMT", "rCr", 'u', unknownDimlet, 'r', Items.redstone, 'T', redstoneTorch, 'M', ModBlocks.machineBase,
                'C', Items.comparator);

        GameRegistry.addRecipe(new ItemStack(emptyDimensionTab), "prp", "rpr", "prp", 'p', Items.paper, 'r', Items.redstone);
        GameRegistry.addRecipe(new ItemStack(dimensionMonitorItem), " u ", "rCr", " u ", 'u', unknownDimlet, 'r', Items.redstone, 'C', Items.comparator);
        GameRegistry.addRecipe(new ItemStack(phasedFieldGeneratorItem), "rsr", "sEs", "rsr", 'E', Items.ender_eye, 'r', Items.redstone, 's', dimensionalShard);

        GameRegistry.addRecipe(new ItemStack(dimletTemplate), "sss", "sps", "sss", 's', dimensionalShard, 'p', Items.paper);

        if (DimletConfiguration.dimensionalShardRecipe) {
            GameRegistry.addRecipe(new ItemStack(dimensionalShard, 8), "deg", "irG", "qcL", 'd', Items.diamond, 'e', Items.emerald, 'g', Items.gold_ingot,
                    'i', Items.iron_ingot, 'r', Items.redstone, 'G', Items.glowstone_dust, 'q', Items.quartz, 'c', Items.coal, 'L', Blocks.glass);
        }
    }
}
