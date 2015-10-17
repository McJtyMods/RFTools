package mcjty.rftools.blocks.screens;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.lib.container.GenericItemBlock;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.crafting.PreservingShapedRecipe;
import mcjty.rftools.items.screenmodules.*;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ScreenSetup {
    public static ScreenBlock screenBlock;
    public static ScreenHitBlock screenHitBlock;
    public static ScreenControllerBlock screenControllerBlock;

    public static TextModuleItem textModuleItem;
    public static EnergyModuleItem energyModuleItem;
    public static EnergyPlusModuleItem energyPlusModuleItem;
    public static DimensionModuleItem dimensionModuleItem;
    public static InventoryModuleItem inventoryModuleItem;
    public static InventoryPlusModuleItem inventoryPlusModuleItem;
    public static ClockModuleItem clockModuleItem;
    public static FluidModuleItem fluidModuleItem;
    public static FluidPlusModuleItem fluidPlusModuleItem;
    public static CounterModuleItem counterModuleItem;
    public static CounterPlusModuleItem counterPlusModuleItem;
    public static RedstoneModuleItem redstoneModuleItem;
    public static MachineInformationModuleItem machineInformationModuleItem;
    public static ComputerModuleItem computerModuleItem;
    public static ButtonModuleItem buttonModuleItem;

    public static void setupBlocks() {
        screenBlock = new ScreenBlock("screenBlock", ScreenTileEntity.class);
        GameRegistry.registerBlock(screenBlock, GenericItemBlock.class, "screenBlock");
        GameRegistry.registerTileEntity(ScreenTileEntity.class, "ScreenTileEntity");

        screenHitBlock = new ScreenHitBlock();
        GameRegistry.registerBlock(screenHitBlock, "screenHitBlock");
        GameRegistry.registerTileEntity(ScreenHitTileEntity.class, "ScreenHitTileEntity");

        screenControllerBlock = new ScreenControllerBlock();
        GameRegistry.registerBlock(screenControllerBlock, GenericItemBlock.class, "screenControllerBlock");
        GameRegistry.registerTileEntity(ScreenControllerTileEntity.class, "ScreenControllerTileEntity");
    }

    public static void setupItems() {
        textModuleItem = new TextModuleItem();
        textModuleItem.setUnlocalizedName("TextModule");
        textModuleItem.setCreativeTab(RFTools.tabRfTools);
        textModuleItem.setTextureName(RFTools.MODID + ":modules/textModuleItem");
        GameRegistry.registerItem(textModuleItem, "textModuleItem");

        inventoryModuleItem = new InventoryModuleItem();
        inventoryModuleItem.setUnlocalizedName("InventoryModule");
        inventoryModuleItem.setCreativeTab(RFTools.tabRfTools);
        inventoryModuleItem.setTextureName(RFTools.MODID + ":modules/inventoryModuleItem");
        GameRegistry.registerItem(inventoryModuleItem, "inventoryModuleItem");

        inventoryPlusModuleItem = new InventoryPlusModuleItem();
        inventoryPlusModuleItem.setUnlocalizedName("InventoryPlusModule");
        inventoryPlusModuleItem.setCreativeTab(RFTools.tabRfTools);
        inventoryPlusModuleItem.setTextureName(RFTools.MODID + ":modules/inventoryPlusModuleItem");
        GameRegistry.registerItem(inventoryPlusModuleItem, "inventoryPlusModuleItem");

        energyModuleItem = new EnergyModuleItem();
        energyModuleItem.setUnlocalizedName("EnergyModule");
        energyModuleItem.setCreativeTab(RFTools.tabRfTools);
        energyModuleItem.setTextureName(RFTools.MODID + ":modules/energyModuleItem");
        GameRegistry.registerItem(energyModuleItem, "energyModuleItem");

        energyPlusModuleItem = new EnergyPlusModuleItem();
        energyPlusModuleItem.setUnlocalizedName("EnergyPlusModule");
        energyPlusModuleItem.setCreativeTab(RFTools.tabRfTools);
        energyPlusModuleItem.setTextureName(RFTools.MODID + ":modules/energyPlusModuleItem");
        GameRegistry.registerItem(energyPlusModuleItem, "energyPlusModuleItem");

        dimensionModuleItem = new DimensionModuleItem();
        dimensionModuleItem.setUnlocalizedName("DimensionModule");
        dimensionModuleItem.setCreativeTab(RFTools.tabRfTools);
        dimensionModuleItem.setTextureName(RFTools.MODID + ":modules/dimensionModuleItem");
        GameRegistry.registerItem(dimensionModuleItem, "dimensionModuleItem");

        clockModuleItem = new ClockModuleItem();
        clockModuleItem.setUnlocalizedName("ClockModule");
        clockModuleItem.setCreativeTab(RFTools.tabRfTools);
        clockModuleItem.setTextureName(RFTools.MODID + ":modules/clockModuleItem");
        GameRegistry.registerItem(clockModuleItem, "clockModuleItem");

        fluidModuleItem = new FluidModuleItem();
        fluidModuleItem.setUnlocalizedName("FluidModule");
        fluidModuleItem.setCreativeTab(RFTools.tabRfTools);
        fluidModuleItem.setTextureName(RFTools.MODID + ":modules/fluidModuleItem");
        GameRegistry.registerItem(fluidModuleItem, "fluidModuleItem");

        fluidPlusModuleItem = new FluidPlusModuleItem();
        fluidPlusModuleItem.setUnlocalizedName("FluidPlusModule");
        fluidPlusModuleItem.setCreativeTab(RFTools.tabRfTools);
        fluidPlusModuleItem.setTextureName(RFTools.MODID + ":modules/fluidPlusModuleItem");
        GameRegistry.registerItem(fluidPlusModuleItem, "fluidPlusModuleItem");

        counterModuleItem = new CounterModuleItem();
        counterModuleItem.setUnlocalizedName("CounterModule");
        counterModuleItem.setCreativeTab(RFTools.tabRfTools);
        counterModuleItem.setTextureName(RFTools.MODID + ":modules/counterModuleItem");
        GameRegistry.registerItem(counterModuleItem, "counterModuleItem");

        counterPlusModuleItem = new CounterPlusModuleItem();
        counterPlusModuleItem.setUnlocalizedName("CounterPlusModule");
        counterPlusModuleItem.setCreativeTab(RFTools.tabRfTools);
        counterPlusModuleItem.setTextureName(RFTools.MODID + ":modules/counterPlusModuleItem");
        GameRegistry.registerItem(counterPlusModuleItem, "counterPlusModuleItem");

        redstoneModuleItem = new RedstoneModuleItem();
        redstoneModuleItem.setUnlocalizedName("RedstoneModule");
        redstoneModuleItem.setCreativeTab(RFTools.tabRfTools);
        redstoneModuleItem.setTextureName(RFTools.MODID + ":modules/redstoneModuleItem");
        GameRegistry.registerItem(redstoneModuleItem, "redstoneModuleItem");

        machineInformationModuleItem = new MachineInformationModuleItem();
        machineInformationModuleItem.setUnlocalizedName("MachineInformationModule");
        machineInformationModuleItem.setCreativeTab(RFTools.tabRfTools);
        machineInformationModuleItem.setTextureName(RFTools.MODID + ":modules/machineInformationModuleItem");
        GameRegistry.registerItem(machineInformationModuleItem, "machineInformationModuleItem");

        computerModuleItem = new ComputerModuleItem();
        computerModuleItem.setUnlocalizedName("ComputerModule");
        computerModuleItem.setCreativeTab(RFTools.tabRfTools);
        computerModuleItem.setTextureName(RFTools.MODID + ":modules/computerModuleItem");
        GameRegistry.registerItem(computerModuleItem, "computerModuleItem");

        buttonModuleItem = new ButtonModuleItem();
        buttonModuleItem.setUnlocalizedName("ButtonModule");
        buttonModuleItem.setCreativeTab(RFTools.tabRfTools);
        buttonModuleItem.setTextureName(RFTools.MODID + ":modules/buttonModuleItem");
        GameRegistry.registerItem(buttonModuleItem, "buttonModuleItem");
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

        GameRegistry.addRecipe(new ItemStack(textModuleItem), " p ", "rir", " b ", 'p', Items.paper, 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);
        GameRegistry.addRecipe(new ItemStack(clockModuleItem), " c ", "rir", " b ", 'c', Items.clock, 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);
        GameRegistry.addRecipe(new ItemStack(energyModuleItem), " r ", "rir", " b ", 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);
        GameRegistry.addRecipe(new ItemStack(dimensionModuleItem), " c ", "rir", " b ", 'c', Items.ender_pearl, 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);
        GameRegistry.addRecipe(new ItemStack(fluidModuleItem), " c ", "rir", " b ", 'c', Items.bucket, 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);
        GameRegistry.addRecipe(new ItemStack(inventoryModuleItem), " c ", "rir", " b ", 'c', Blocks.chest, 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);
        GameRegistry.addRecipe(new ItemStack(counterModuleItem), " c ", "rir", " b ", 'c', Items.comparator, 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);
        GameRegistry.addRecipe(new ItemStack(redstoneModuleItem), " c ", "rir", " b ", 'c', Items.repeater, 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);
        GameRegistry.addRecipe(new ItemStack(machineInformationModuleItem), " f ", "rir", " b ", 'f', Blocks.furnace, 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);
        GameRegistry.addRecipe(new ItemStack(computerModuleItem), " f ", "rir", " b ", 'f', Blocks.quartz_block, 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);
        GameRegistry.addRecipe(new ItemStack(buttonModuleItem), " f ", "rir", " b ", 'f', Blocks.stone_button, 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);
        GameRegistry.addRecipe(new ItemStack(buttonModuleItem), "b", 'b', buttonModuleItem);    // To clear it

        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                null, new ItemStack(Items.ender_pearl), null,
                new ItemStack(Items.gold_ingot), new ItemStack(energyModuleItem), new ItemStack(Items.gold_ingot),
                null, new ItemStack(Items.ender_pearl), null }, new ItemStack(energyPlusModuleItem), 4));
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                null, new ItemStack(Items.ender_pearl), null,
                new ItemStack(Items.gold_ingot), new ItemStack(fluidModuleItem), new ItemStack(Items.gold_ingot),
                null, new ItemStack(Items.ender_pearl), null }, new ItemStack(fluidPlusModuleItem), 4));
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                null, new ItemStack(Items.ender_pearl), null,
                new ItemStack(Items.gold_ingot), new ItemStack(inventoryModuleItem), new ItemStack(Items.gold_ingot),
                null, new ItemStack(Items.ender_pearl), null }, new ItemStack(inventoryPlusModuleItem), 4));
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                null, new ItemStack(Items.ender_pearl), null,
                new ItemStack(Items.gold_ingot), new ItemStack(counterModuleItem), new ItemStack(Items.gold_ingot),
                null, new ItemStack(Items.ender_pearl), null }, new ItemStack(counterPlusModuleItem), 4));
    }
}
