package mcjty.rftools.blocks.screens;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.container.GenericItemBlock;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.crafting.PreservingShapedRecipe;
import mcjty.rftools.items.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ScreenSetup {
    public static ScreenBlock screenBlock;
    public static ScreenControllerBlock screenControllerBlock;

    public static void setupBlocks() {
        screenBlock = new ScreenBlock("screenBlock", ScreenTileEntity.class);
        GameRegistry.registerBlock(screenBlock, GenericItemBlock.class, "screenBlock");
        GameRegistry.registerTileEntity(ScreenTileEntity.class, "ScreenTileEntity");

        screenControllerBlock = new ScreenControllerBlock();
        GameRegistry.registerBlock(screenControllerBlock, GenericItemBlock.class, "screenControllerBlock");
        GameRegistry.registerTileEntity(ScreenControllerTileEntity.class, "ScreenControllerTileEntity");
    }

    public static void setupCrafting() {
        GameRegistry.addRecipe(new ItemStack(screenControllerBlock), "ror", "gMg", "rgr", 'r', Items.redstone, 'o', Items.ender_pearl, 'M', ModBlocks.machineFrame,
                'g', Blocks.glass);
        GameRegistry.addRecipe(new ItemStack(screenBlock), "ggg", "gMg", "iii", 'M', ModBlocks.machineBase,
                'g', Blocks.glass, 'i', Items.iron_ingot);

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
        GameRegistry.addRecipe(new ItemStack(ModItems.machineInformationModuleItem), " f ", "rir", " b ", 'f', Blocks.furnace, 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);
        GameRegistry.addRecipe(new ItemStack(ModItems.computerModuleItem), " f ", "rir", " b ", 'f', Blocks.quartz_block, 'r', Items.redstone, 'i', Items.iron_ingot,
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
}
