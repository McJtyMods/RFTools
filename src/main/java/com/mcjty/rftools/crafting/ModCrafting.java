package com.mcjty.rftools.crafting;

import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.blocks.dimletconstruction.DimletConstructionConfiguration;
import com.mcjty.rftools.items.ModItems;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
        GameRegistry.addRecipe(new ItemStack(ModBlocks.liquidMonitorBlock), " T ", "bMb", " T ", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'b', Items.bucket);

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

        initLogicBlockCrafting();

        GameRegistry.addRecipe(new ItemStack(ModBlocks.endergenicBlock), "DoD", "oMo", "DoD", 'M', ModBlocks.machineFrame, 'D', Items.diamond, 'o', Items.ender_pearl);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.pearlInjectorBlock), " C ", "rMr", " H ", 'C', Blocks.chest, 'r', Items.redstone,
                'M', ModBlocks.machineFrame, 'H', Blocks.hopper);

        GameRegistry.addRecipe(new ItemStack(ModBlocks.shieldBlock), "gTg", "rMr", "ooo", 'M', ModBlocks.machineFrame, 'o', Blocks.obsidian,
                'r', Items.redstone, 'T', redstoneTorch, 'g', Items.gold_ingot);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.shieldBlock2), "ss ", "ss ", "   ", 's', ModBlocks.shieldBlock);
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

        GameRegistry.addRecipe(new ItemStack(ModBlocks.environmentalControllerBlock), "oDo", "GMI", "oEo", 'o', Items.ender_pearl, 'M', ModBlocks.machineFrame,
                'D', Blocks.diamond_block, 'E', Blocks.emerald_block, 'G', Blocks.gold_block, 'I', Blocks.iron_block);

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

        initScreenCrafting();

        GameRegistry.addRecipe(new ItemStack(ModItems.dimletTemplate), "sss", "sps", "sss", 's', ModItems.dimensionalShard, 'p', Items.paper);

        initDimletConstructionCrafting();
        initEnvModuleCrafting();
    }

    private static void initEnvModuleCrafting() {
        Object inkSac = Item.itemRegistry.getObjectById(351);

        ItemStack ironGolemSyringe = createMobSyringe("Iron Golem");
        ItemStack horseSyringe = createMobSyringe("Horse");
        ItemStack zombieSyringe = createMobSyringe("Zombie");
        ItemStack diamondPick = createEnchantedItem(Items.diamond_pickaxe, Enchantment.efficiency.effectId, 3);

        GameRegistry.addRecipe(new ItemStack(ModItems.regenerationEModuleItem), " p ", "rgr", " b ", 'p', ironGolemSyringe, 'r', Items.redstone, 'g', Items.gold_ingot,
                'b', inkSac);
        GameRegistry.addRecipe(new ItemStack(ModItems.speedEModuleItem), " p ", "rgr", " b ", 'p', horseSyringe, 'r', Items.redstone, 'g', Items.gold_ingot,
                'b', inkSac);

        GameRegistry.addRecipe(new ItemStack(ModItems.hasteEModuleItem), " p ", "rgr", " b ", 'p', diamondPick, 'r', Items.redstone, 'g', Items.gold_ingot,
                'b', inkSac);
        GameRegistry.addRecipe(new ItemStack(ModItems.saturationEModuleItem), " p ", "rgr", " b ", 'p', zombieSyringe, 'r', Items.redstone, 'g', Items.gold_ingot,
                'b', inkSac);

        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.regenerationPlusEModuleItem), ironGolemSyringe, ironGolemSyringe, new ItemStack(ModItems.regenerationEModuleItem));
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.speedPlusEModuleItem), horseSyringe, horseSyringe, new ItemStack(ModItems.speedEModuleItem));
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.hastePlusEModuleItem), diamondPick, new ItemStack(ModItems.hasteEModuleItem));
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.saturationPlusEModuleItem), zombieSyringe, zombieSyringe, new ItemStack(ModItems.saturationEModuleItem));
    }

    private static ItemStack createMobSyringe(String mobName) {
        ItemStack syringe = new ItemStack(ModItems.syringeItem);
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setString("mobName", mobName);
        tagCompound.setInteger("level", DimletConstructionConfiguration.maxMobInjections);
        syringe.setTagCompound(tagCompound);
        return syringe;
    }


    private static void initLogicBlockCrafting() {
        Object redstoneTorch = Item.itemRegistry.getObject("redstone_torch");
        GameRegistry.addRecipe(new ItemStack(ModBlocks.sequencerBlock), "rTr", "TMT", "rTr", 'r', Items.redstone, 'T', redstoneTorch, 'M', ModBlocks.machineBase);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.counterBlock), "gcg", "TMT", "rTr", 'c', Items.clock, 'r', Items.redstone, 'T', redstoneTorch, 'M', ModBlocks.machineBase, 'g', Items.gold_nugget);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.timerBlock), "rcr", "TMT", "rTr", 'c', Items.clock, 'r', Items.redstone, 'T', redstoneTorch, 'M', ModBlocks.machineBase);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.enderMonitorBlock), "ror", "TMT", "rTr", 'o', Items.ender_pearl, 'r', Items.redstone, 'T', redstoneTorch, 'M', ModBlocks.machineBase);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.dimensionMonitorBlock), " u ", "TMT", "rCr", 'u', ModItems.unknownDimlet, 'r', Items.redstone, 'T', redstoneTorch, 'M', ModBlocks.machineBase,
                'C', Items.comparator);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.redstoneTransmitterBlock), "ror", "TMT", "rRr", 'o', Items.ender_pearl, 'r', Items.redstone, 'T', redstoneTorch, 'R', Blocks.redstone_block, 'M', ModBlocks.machineBase);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.redstoneReceiverBlock), "ror", "TMT", "rRr", 'o', Items.ender_pearl, 'r', Items.redstone, 'T', Items.comparator, 'R', Blocks.redstone_block, 'M', ModBlocks.machineBase);
    }

    private static void initDimletConstructionCrafting() {
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(ModItems.peaceEssenceItem),
                createMobEssenceItemStack("Iron Golem"),
                createMobEssenceItemStack("Enderman"),
                createMobEssenceItemStack("Snowman"),
                createMobEssenceItemStack("Bat"),
                createMobEssenceItemStack("Ocelot"),
                createMobEssenceItemStack("Squid"),
                createMobEssenceItemStack("Wolf"),
                createMobEssenceItemStack("Zombie Pigman"),
                createMobEssenceItemStack("Mooshroom")));

        GameRegistry.addRecipe(new ItemStack(ModBlocks.dimletWorkbenchBlock), "gug", "cMc", "grg", 'M', ModBlocks.machineFrame, 'u', ModItems.unknownDimlet, 'c', Blocks.crafting_table,
                'r', Items.redstone, 'g', Items.gold_nugget);

        GameRegistry.addRecipe(new ItemStack(ModBlocks.biomeAbsorberBlock), "dws", "wMw", "swd", 'M', ModBlocks.machineFrame, 'd', Blocks.dirt, 's', Blocks.sapling, 'w', Blocks.wool);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.materialAbsorberBlock), "dwc", "wMw", "swg", 'M', ModBlocks.machineFrame, 'd', Blocks.dirt, 'c', Blocks.cobblestone, 's', Blocks.sand,
                'g', Blocks.gravel, 'w', Blocks.wool);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.liquidAbsorberBlock), "bwb", "wMw", "bwb", 'M', ModBlocks.machineFrame, 'b', Items.bucket, 'w', Blocks.wool);
        GameRegistry.addRecipe(new ItemStack(ModItems.syringeItem), "i  ", " i ", "  b", 'i', Items.iron_ingot, 'b', Items.glass_bottle);

        ItemStack diamondPick = createEnchantedItem(Items.diamond_pickaxe, Enchantment.efficiency.effectId, 3);
        GameRegistry.addRecipe(new ItemStack(ModItems.efficiencyEssenceItem), " p ", "ese", " e ", 'p', diamondPick, 's', Items.nether_star, 'e', Items.ender_eye);

        ItemStack ironPick = createEnchantedItem(Items.iron_pickaxe, Enchantment.efficiency.effectId, 2);
        GameRegistry.addRecipe(new ItemStack(ModItems.mediocreEfficiencyEssenceItem), " p ", "ese", " e ", 'p', ironPick, 's', Items.ghast_tear, 'e', Items.ender_eye);
    }

    private static ItemStack createEnchantedItem(Item item, int effectId, int amount) {
        ItemStack stack = new ItemStack(item);
        Map enchant = new HashMap();
        enchant.put(effectId, amount);
        EnchantmentHelper.setEnchantments(enchant, stack);
        return stack;
    }

    private static void initScreenCrafting() {
        GameRegistry.addRecipe(new ItemStack(ModBlocks.screenControllerBlock), "ror", "gMg", "rgr", 'r', Items.redstone, 'o', Items.ender_pearl, 'M', ModBlocks.machineFrame,
                'g', Blocks.glass);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.screenBlock), "ggg", "gMg", "iii", 'M', ModBlocks.machineBase,
                'g', Blocks.glass, 'i', Items.iron_ingot);

        GameRegistry.addRecipe(new PreservingShapedRecipe(2, 2, new ItemStack[]{
                new ItemStack(ModBlocks.screenBlock), new ItemStack(Items.iron_ingot),
                new ItemStack(Items.iron_ingot), new ItemStack(Items.iron_ingot)
        }, new ItemStack(ModBlocks.screenBlock), 0, Collections.<String, Object>singletonMap("large", Boolean.TRUE)));
        GameRegistry.addRecipe(new PreservingShapedRecipe(2, 2, new ItemStack[] {
                new ItemStack(ModBlocks.screenBlock), new ItemStack(Blocks.glass),
                new ItemStack(Blocks.glass), new ItemStack(Blocks.glass)
        }, new ItemStack(ModBlocks.screenBlock), 0, Collections.<String, Object>singletonMap("transparent", Boolean.TRUE)));
        Map<String,Object> flags = new HashMap<String, Object>();
        flags.put("large", Boolean.FALSE);
        flags.put("transparent", Boolean.FALSE);
        GameRegistry.addRecipe(new PreservingShapedRecipe(1, 1, new ItemStack[] {
                new ItemStack(ModBlocks.screenBlock)
        }, new ItemStack(ModBlocks.screenBlock), 0, flags));

        initScreenModuleCrafting();
    }

    private static void initScreenModuleCrafting() {
        Object inkSac = Item.itemRegistry.getObjectById(351);

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
        GameRegistry.addRecipe(new ItemStack(ModItems.inventoryModuleItem), " c ", "rir", " b ", 'c', Blocks.chest, 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);
        GameRegistry.addRecipe(new ItemStack(ModItems.counterModuleItem), " c ", "rir", " b ", 'c', Items.comparator, 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);
        GameRegistry.addRecipe(new ItemStack(ModItems.redstoneModuleItem), " c ", "rir", " b ", 'c', Items.repeater, 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);

        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                null, new ItemStack(Items.ender_pearl), null,
                new ItemStack(Items.gold_ingot), new ItemStack(ModItems.energyModuleItem), new ItemStack(Items.gold_ingot),
                null, new ItemStack(Items.ender_pearl), null }, new ItemStack(ModItems.energyPlusModuleItem), 4));
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                null, new ItemStack(Items.ender_pearl), null,
                new ItemStack(Items.gold_ingot), new ItemStack(ModItems.fluidModuleItem), new ItemStack(Items.gold_ingot),
                null, new ItemStack(Items.ender_pearl), null }, new ItemStack(ModItems.fluidPlusModuleItem), 4));
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                null, new ItemStack(Items.ender_pearl), null,
                new ItemStack(Items.gold_ingot), new ItemStack(ModItems.inventoryModuleItem), new ItemStack(Items.gold_ingot),
                null, new ItemStack(Items.ender_pearl), null }, new ItemStack(ModItems.inventoryPlusModuleItem), 4));
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                null, new ItemStack(Items.ender_pearl), null,
                new ItemStack(Items.gold_ingot), new ItemStack(ModItems.counterModuleItem), new ItemStack(Items.gold_ingot),
                null, new ItemStack(Items.ender_pearl), null }, new ItemStack(ModItems.counterPlusModuleItem), 4));
    }

    private static ItemStack createMobEssenceItemStack(String mobName) {
        ItemStack syringeIronGolem = new ItemStack(ModItems.syringeItem);
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setInteger("level", DimletConstructionConfiguration.maxMobInjections);
        tagCompound.setString("mobName", mobName);
        syringeIronGolem.setTagCompound(tagCompound);
        return syringeIronGolem;
    }

}
