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
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ScreenSetup {
    public static ScreenBlock screenBlock;
    public static ScreenBlock creativeScreenBlock;
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
    public static ElevatorButtonModuleItem elevatorButtonModuleItem;
    public static RedstoneModuleItem redstoneModuleItem;
    public static CounterModuleItem counterModuleItem;
    public static CounterPlusModuleItem counterPlusModuleItem;
    public static StorageControlModuleItem storageControlModuleItem;
    public static DumpModuleItem dumpModuleItem;

    public static void init() {
        screenBlock = new ScreenBlock("screen", ScreenTileEntity.class);
        creativeScreenBlock = new ScreenBlock("creative_screen", CreativeScreenTileEntity.class) {
            @Override
            public boolean isCreative() {
                return true;
            }
        };
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
        elevatorButtonModuleItem = new ElevatorButtonModuleItem();
        redstoneModuleItem = new RedstoneModuleItem();
        counterModuleItem = new CounterModuleItem();
        counterPlusModuleItem = new CounterPlusModuleItem();
        storageControlModuleItem = new StorageControlModuleItem();
        dumpModuleItem = new DumpModuleItem();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        screenBlock.initModel();
        creativeScreenBlock.initModel();
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
        elevatorButtonModuleItem.initModel();
        redstoneModuleItem.initModel();
        counterModuleItem.initModel();
        counterPlusModuleItem.initModel();
        storageControlModuleItem.initModel();
        dumpModuleItem.initModel();
    }

    public static void initCrafting() {
        GameRegistry.addRecipe(new ItemStack(screenControllerBlock), "ror", "gMg", "rgr", 'r', Items.REDSTONE, 'o', Items.ENDER_PEARL, 'M', ModBlocks.machineFrame,
                               'g', Blocks.GLASS);
        GameRegistry.addRecipe(new ItemStack(screenBlock), "ggg", "gMg", "iii", 'M', ModBlocks.machineBase,
                'g', Blocks.GLASS, 'i', Items.IRON_INGOT);

        initScreenModuleCrafting();
    }

    private static void initScreenModuleCrafting() {
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(textModuleItem), " p ", "rir", " b ", 'p', Items.PAPER, 'r', Items.REDSTONE, 'i', Items.IRON_INGOT,
                'b', "dyeBlack"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(clockModuleItem), " c ", "rir", " b ", 'c', Items.CLOCK, 'r', Items.REDSTONE, 'i', Items.IRON_INGOT,
                'b', "dyeBlack"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(energyModuleItem), " r ", "rir", " b ", 'r', Items.REDSTONE, 'i', Items.IRON_INGOT,
                'b', "dyeBlack"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(fluidModuleItem), " c ", "rir", " b ", 'c', Items.BUCKET, 'r', Items.REDSTONE, 'i', Items.IRON_INGOT,
                'b', "dyeBlack"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(inventoryModuleItem), " c ", "rir", " b ", 'c', "chest", 'r', Items.REDSTONE, 'i', Items.IRON_INGOT,
                'b', "dyeBlack"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(counterModuleItem), " c ", "rir", " b ", 'c', Items.COMPARATOR, 'r', Items.REDSTONE, 'i', Items.IRON_INGOT,
                'b', "dyeBlack"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(redstoneModuleItem), " c ", "rir", " b ", 'c', Items.REPEATER, 'r', Items.REDSTONE, 'i', Items.IRON_INGOT,
                'b', "dyeBlack"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(machineInformationModuleItem), " f ", "rir", " b ", 'f', Blocks.FURNACE, 'r', Items.REDSTONE, 'i', Items.IRON_INGOT,
                'b', "dyeBlack"));
//        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(computerModuleItem), " f ", "rir", " b ", 'f', Blocks.quartz_block, 'r', Items.REDSTONE, 'i', Items.IRON_INGOT,
//                'b', "dyeBlack"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(buttonModuleItem), " f ", "rir", " b ", 'f', Blocks.STONE_BUTTON, 'r', Items.REDSTONE, 'i', Items.IRON_INGOT,
                               'b', "dyeBlack"));
        GameRegistry.addRecipe(new ItemStack(buttonModuleItem), "b", 'b', buttonModuleItem);    // To clear it

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(elevatorButtonModuleItem), "fff", "rir", " b ", 'f', Blocks.STONE_BUTTON, 'r', Items.REDSTONE, 'i', Items.IRON_INGOT,
                               'b', "dyeBlack"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(storageControlModuleItem), " c ", "rir", " b ", 'c', Blocks.CRAFTING_TABLE, 'r', Items.REDSTONE, 'i', Items.IRON_INGOT,
                'b', "dyeBlack"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(dumpModuleItem), " c ", "rir", " b ", 'c', Blocks.WOODEN_BUTTON, 'r', Items.REDSTONE, 'i', Items.IRON_INGOT,
                'b', "dyeBlack"));

        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                null, new ItemStack(Items.ENDER_PEARL), null,
                new ItemStack(Items.GOLD_INGOT), new ItemStack(energyModuleItem), new ItemStack(Items.GOLD_INGOT),
                null, new ItemStack(Items.ENDER_PEARL), null }, new ItemStack(energyPlusModuleItem), 4));
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                null, new ItemStack(Items.ENDER_PEARL), null,
                new ItemStack(Items.GOLD_INGOT), new ItemStack(fluidModuleItem), new ItemStack(Items.GOLD_INGOT),
                null, new ItemStack(Items.ENDER_PEARL), null }, new ItemStack(fluidPlusModuleItem), 4));
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                null, new ItemStack(Items.ENDER_PEARL), null,
                new ItemStack(Items.GOLD_INGOT), new ItemStack(inventoryModuleItem), new ItemStack(Items.GOLD_INGOT),
                null, new ItemStack(Items.ENDER_PEARL), null }, new ItemStack(inventoryPlusModuleItem), 4));
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                null, new ItemStack(Items.ENDER_PEARL), null,
                new ItemStack(Items.GOLD_INGOT), new ItemStack(counterModuleItem), new ItemStack(Items.GOLD_INGOT),
                null, new ItemStack(Items.ENDER_PEARL), null }, new ItemStack(counterPlusModuleItem), 4));
    }
}
