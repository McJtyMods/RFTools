package mcjty.rftools.blocks.dimletconstruction;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.container.GenericItemBlock;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.environmental.EnvironmentalSetup;
import mcjty.rftools.crafting.NBTMatchingRecipe;
import mcjty.rftools.items.ModItems;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class DimletConstructionSetup {
    public static DimletWorkbenchBlock dimletWorkbenchBlock;
    public static BiomeAbsorberBlock biomeAbsorberBlock;
    public static MaterialAbsorberBlock materialAbsorberBlock;
    public static LiquidAbsorberBlock liquidAbsorberBlock;
    public static TimeAbsorberBlock timeAbsorberBlock;

    public static void setupBlocks() {
        dimletWorkbenchBlock = new DimletWorkbenchBlock();
        GameRegistry.registerBlock(dimletWorkbenchBlock, GenericItemBlock.class, "dimletWorkbenchBlock");
        GameRegistry.registerTileEntity(DimletWorkbenchTileEntity.class, "DimletWorkbenchTileEntity");

        biomeAbsorberBlock = new BiomeAbsorberBlock();
        GameRegistry.registerBlock(biomeAbsorberBlock, GenericItemBlock.class, "biomeAbsorberBlock");
        GameRegistry.registerTileEntity(BiomeAbsorberTileEntity.class, "BiomeAbsorberTileEntity");

        materialAbsorberBlock = new MaterialAbsorberBlock();
        GameRegistry.registerBlock(materialAbsorberBlock, GenericItemBlock.class, "materialAbsorberBlock");
        GameRegistry.registerTileEntity(MaterialAbsorberTileEntity.class, "MaterialAbsorberTileEntity");

        liquidAbsorberBlock = new LiquidAbsorberBlock();
        GameRegistry.registerBlock(liquidAbsorberBlock, GenericItemBlock.class, "liquidAbsorberBlock");
        GameRegistry.registerTileEntity(LiquidAbsorberTileEntity.class, "LiquidAbsorberTileEntity");

        timeAbsorberBlock = new TimeAbsorberBlock();
        GameRegistry.registerBlock(timeAbsorberBlock, GenericItemBlock.class, "timeAbsorberBlock");
        GameRegistry.registerTileEntity(TimeAbsorberTileEntity.class, "TimeAbsorberTileEntity");
    }

    public static void setupCrafting() {
        GameRegistry.addRecipe(new ItemStack(dimletWorkbenchBlock), "gug", "cMc", "grg", 'M', ModBlocks.machineFrame, 'u', ModItems.unknownDimlet, 'c', Blocks.crafting_table,
                'r', Items.redstone, 'g', Items.gold_nugget);

        GameRegistry.addRecipe(new ItemStack(biomeAbsorberBlock), "dws", "wMw", "swd", 'M', ModBlocks.machineFrame, 'd', Blocks.dirt, 's', Blocks.sapling, 'w', Blocks.wool);
        GameRegistry.addShapelessRecipe(new ItemStack(biomeAbsorberBlock), new ItemStack(biomeAbsorberBlock));
        GameRegistry.addRecipe(new ItemStack(materialAbsorberBlock), "dwc", "wMw", "swg", 'M', ModBlocks.machineFrame, 'd', Blocks.dirt, 'c', Blocks.cobblestone, 's', Blocks.sand,
                'g', Blocks.gravel, 'w', Blocks.wool);
        GameRegistry.addShapelessRecipe(new ItemStack(materialAbsorberBlock), new ItemStack(materialAbsorberBlock));
        GameRegistry.addRecipe(new ItemStack(liquidAbsorberBlock), "bwb", "wMw", "bwb", 'M', ModBlocks.machineFrame, 'b', Items.bucket, 'w', Blocks.wool);
        GameRegistry.addShapelessRecipe(new ItemStack(liquidAbsorberBlock), new ItemStack(liquidAbsorberBlock));
        GameRegistry.addRecipe(new ItemStack(timeAbsorberBlock), "cwc", "wMw", "cwc", 'M', ModBlocks.machineFrame, 'c', Items.clock, 'w', Blocks.wool);
        GameRegistry.addShapelessRecipe(new ItemStack(timeAbsorberBlock), new ItemStack(timeAbsorberBlock));
    }

    public static void initDimletConstructionCrafting() {
        String[] syringeMatcher = new String[] { "level", "mobName" };
        String[] pickMatcher = new String[] { "ench" };

        GameRegistry.addRecipe(new NBTMatchingRecipe(3, 3,
                new ItemStack[]{EnvironmentalSetup.createMobSyringe("Iron Golem"), EnvironmentalSetup.createMobSyringe("Enderman"), EnvironmentalSetup.createMobSyringe("Snowman"),
                        EnvironmentalSetup.createMobSyringe("Bat"), EnvironmentalSetup.createMobSyringe("Ocelot"), EnvironmentalSetup.createMobSyringe("Squid"),
                        EnvironmentalSetup.createMobSyringe("Wolf"), EnvironmentalSetup.createMobSyringe("Zombie Pigman"), EnvironmentalSetup.createMobSyringe("Mooshroom")},
                new String[][]{syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher},
                new ItemStack(ModItems.peaceEssenceItem)));

        setupCrafting();

        GameRegistry.addRecipe(new ItemStack(ModItems.syringeItem), "i  ", " i ", "  b", 'i', Items.iron_ingot, 'b', Items.glass_bottle);


        ItemStack diamondPick = EnvironmentalSetup.createEnchantedItem(Items.diamond_pickaxe, Enchantment.efficiency.effectId, 3);
        GameRegistry.addRecipe(new NBTMatchingRecipe(3, 3,
                new ItemStack[] {null, diamondPick, null,
                        new ItemStack(Items.ender_eye), new ItemStack(Items.nether_star), new ItemStack(Items.ender_eye),
                        null, new ItemStack(Items.ender_eye), null},
                new String[][] {null, pickMatcher, null, null, null, null, null, null, null},
                new ItemStack(ModItems.efficiencyEssenceItem)));

        ItemStack ironPick = EnvironmentalSetup.createEnchantedItem(Items.iron_pickaxe, Enchantment.efficiency.effectId, 2);
        GameRegistry.addRecipe(new NBTMatchingRecipe(3, 3,
                new ItemStack[] {null, ironPick, null,
                        new ItemStack(Items.ender_eye), new ItemStack(Items.ghast_tear), new ItemStack(Items.ender_eye),
                        null, new ItemStack(Items.ender_eye), null},
                new String[][] {null, pickMatcher, null, null, null, null, null, null, null},
                new ItemStack(ModItems.mediocreEfficiencyEssenceItem)));
    }
}
