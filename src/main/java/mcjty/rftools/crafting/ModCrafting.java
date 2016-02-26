package mcjty.rftools.crafting;

import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.crafter.CrafterSetup;
import mcjty.rftools.blocks.generator.CoalGeneratorSetup;
import mcjty.rftools.blocks.infuser.MachineInfuserSetup;
import mcjty.rftools.blocks.logic.LogicBlockSetup;
import mcjty.rftools.blocks.monitor.MonitorSetup;
import mcjty.rftools.blocks.powercell.PowerCellSetup;
import mcjty.rftools.blocks.relay.RelaySetup;
import mcjty.rftools.blocks.screens.ScreenSetup;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.blocks.storage.ModularStorageSetup;
import mcjty.rftools.blocks.teleporter.TeleporterSetup;
import mcjty.rftools.items.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;

public final class ModCrafting {
    static {
        RecipeSorter.register("rftools:shapedpreserving", PreservingShapedRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register("rftools:containeranditem", ContainerAndItemRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register("rftools:containertoitem", ContainerToItemRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register("rftools:nbtmatchingrecipe", NBTMatchingRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
    }

    public static void init() {
        initBaseCrafting();

        CoalGeneratorSetup.initCrafting();
        CrafterSetup.initCrafting();
        ModularStorageSetup.initCrafting();
        TeleporterSetup.initCrafting();
        ScreenSetup.initCrafting();
        LogicBlockSetup.initCrafting();
        MachineInfuserSetup.initCrafting();
        BuilderSetup.initCrafting();
        PowerCellSetup.initCrafting();
        RelaySetup.initCrafting();
        MonitorSetup.initCrafting();
    }

    private static void initBaseCrafting() {
        ItemStack lapisStack = new ItemStack(Items.dye, 1, 4);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.machineFrame), "ili", "g g", "ili", 'i', Items.iron_ingot, 'g', Items.gold_nugget, 'l', lapisStack);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.machineBase), "   ", "ggg", "sss", 'g', Items.gold_nugget, 's', Blocks.stone);

        GameRegistry.addRecipe(new ItemStack(ModItems.rfToolsManualItem), " r ", "rbr", " r ", 'r', Items.redstone, 'b', Items.book);
        GameRegistry.addRecipe(new ItemStack(ModItems.smartWrenchItem), "  i", " l ", "l  ", 'i', Items.iron_ingot, 'l', lapisStack);
        GameRegistry.addRecipe(new ItemStack(ModItems.infusedDiamond), "sss", "sds", "sss", 's', ModItems.dimensionalShardItem, 'd', Items.diamond);

        int dimShardCraftability;
        if (Loader.isModLoaded("rftoolsdim")) {
            dimShardCraftability = GeneralConfiguration.dimensionalShardRecipeWithDimensions;
        } else {
            dimShardCraftability = GeneralConfiguration.dimensionalShardRecipeWithoutDimensions;
        }

        switch (dimShardCraftability) {
            case GeneralConfiguration.CRAFT_NONE:
                break;
            case GeneralConfiguration.CRAFT_EASY:
                GameRegistry.addRecipe(new ItemStack(ModItems.dimensionalShardItem, 8), " d ", "irg", " q ", 'd', Items.diamond, 'g', Items.gold_ingot,
                                       'i', Items.iron_ingot, 'q', Items.quartz);
                break;
            case GeneralConfiguration.CRAFT_HARD:
                GameRegistry.addRecipe(new ItemStack(ModItems.dimensionalShardItem, 8), "deg", "irG", "qcL", 'd', Items.diamond, 'e', Items.emerald, 'g', Items.gold_ingot,
                                       'i', Items.iron_ingot, 'r', Items.redstone, 'G', Items.glowstone_dust, 'q', Items.quartz, 'c', Items.prismarine_shard, 'L', Blocks.glass);
                break;
        }
    }
}
