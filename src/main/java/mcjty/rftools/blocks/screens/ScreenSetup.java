package mcjty.rftools.blocks.screens;

import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.crafting.PreservingShapedRecipe;
import mcjty.rftools.items.screenmodules.*;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ScreenSetup {
    public static ScreenBlock screenBlock;
    public static ScreenHitBlock screenHitBlock;
    public static ScreenControllerBlock screenControllerBlock;

    public static TextModuleItem textModuleItem;
    public static EnergyModuleItem energyModuleItem;
    public static EnergyPlusModuleItem energyPlusModuleItem;
    public static InventoryModuleItem inventoryModuleItem;
    public static InventoryPlusModuleItem inventoryPlusModuleItem;
    public static ClockModuleItem clockModuleItem;
    public static FluidModuleItem fluidModuleItem;
    public static FluidPlusModuleItem fluidPlusModuleItem;
    public static MachineInformationModuleItem machineInformationModuleItem;
    public static ButtonModuleItem buttonModuleItem;
    public static RedstoneModuleItem redstoneModuleItem;
    public static CounterModuleItem counterModuleItem;
    public static CounterPlusModuleItem counterPlusModuleItem;

    public static void init() {
        screenBlock = new ScreenBlock();
        screenHitBlock = new ScreenHitBlock();
        screenControllerBlock = new ScreenControllerBlock();

        textModuleItem = new TextModuleItem();
        inventoryModuleItem = new InventoryModuleItem();
        inventoryPlusModuleItem = new InventoryPlusModuleItem();
        energyModuleItem = new EnergyModuleItem();
        energyPlusModuleItem = new EnergyPlusModuleItem();
        clockModuleItem = new ClockModuleItem();
        fluidModuleItem = new FluidModuleItem();
        fluidPlusModuleItem = new FluidPlusModuleItem();
        machineInformationModuleItem = new MachineInformationModuleItem();
        buttonModuleItem = new ButtonModuleItem();
        redstoneModuleItem = new RedstoneModuleItem();
        counterModuleItem = new CounterModuleItem();
        counterPlusModuleItem = new CounterPlusModuleItem();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        screenBlock.initModel();
        screenHitBlock.initModel();
        screenControllerBlock.initModel();

        textModuleItem.initModel();
        inventoryModuleItem.initModel();
        inventoryPlusModuleItem.initModel();
        energyModuleItem.initModel();
        energyPlusModuleItem.initModel();
        clockModuleItem.initModel();
        fluidModuleItem.initModel();
        fluidPlusModuleItem.initModel();
        machineInformationModuleItem.initModel();
        buttonModuleItem.initModel();
        redstoneModuleItem.initModel();
        counterModuleItem.initModel();
        counterPlusModuleItem.initModel();
    }

    public static void initCrafting() {
        GameRegistry.addRecipe(new ItemStack(screenControllerBlock), "ror", "gMg", "rgr", 'r', Items.redstone, 'o', Items.ender_pearl, 'M', ModBlocks.machineFrame,
                               'g', Blocks.glass);
        GameRegistry.addRecipe(new ItemStack(screenBlock), "ggg", "gMg", "iii", 'M', ModBlocks.machineBase,
                'g', Blocks.glass, 'i', Items.iron_ingot);

        initScreenModuleCrafting();
    }

    private static void initScreenModuleCrafting() {
        ItemStack inkSac = new ItemStack(Items.dye, 1, 0);

        GameRegistry.addRecipe(new ItemStack(textModuleItem), " p ", "rir", " b ", 'p', Items.paper, 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);
        GameRegistry.addRecipe(new ItemStack(clockModuleItem), " c ", "rir", " b ", 'c', Items.clock, 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);
        GameRegistry.addRecipe(new ItemStack(energyModuleItem), " r ", "rir", " b ", 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', inkSac);
//        GameRegistry.addRecipe(new ItemStack(dimensionModuleItem), " c ", "rir", " b ", 'c', Items.ender_pearl, 'r', Items.redstone, 'i', Items.iron_ingot,
//                'b', inkSac);
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
//        GameRegistry.addRecipe(new ItemStack(computerModuleItem), " f ", "rir", " b ", 'f', Blocks.quartz_block, 'r', Items.redstone, 'i', Items.iron_ingot,
//                'b', inkSac);
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
