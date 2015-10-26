package mcjty.rftools.crafting;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.blockprotector.BlockProtectorSetup;
import mcjty.rftools.blocks.crafter.CrafterSetup;
import mcjty.rftools.blocks.dimletconstruction.DimletConstructionSetup;
import mcjty.rftools.blocks.dimlets.DimletSetup;
import mcjty.rftools.blocks.endergen.EndergenicSetup;
import mcjty.rftools.blocks.environmental.EnvironmentalSetup;
import mcjty.rftools.blocks.infuser.MachineInfuserSetup;
import mcjty.rftools.blocks.itemfilter.ItemFilterSetup;
import mcjty.rftools.blocks.logic.LogicBlockSetup;
import mcjty.rftools.blocks.monitor.MonitorSetup;
import mcjty.rftools.blocks.relay.RelaySetup;
import mcjty.rftools.blocks.screens.ScreenSetup;
import mcjty.rftools.blocks.security.SecuritySetup;
import mcjty.rftools.blocks.shield.ShieldSetup;
import mcjty.rftools.blocks.spaceprojector.SpaceProjectorSetup;
import mcjty.rftools.blocks.spawner.SpawnerSetup;
import mcjty.rftools.blocks.storage.ModularStorageSetup;
import mcjty.rftools.blocks.storagemonitor.StorageScannerSetup;
import mcjty.rftools.blocks.teleporter.TeleporterSetup;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.items.shapecard.ShapeCardItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.RecipeSorter;

public final class ModCrafting {
    static {
        RecipeSorter.register("rftools:shapedpreserving", PreservingShapedRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register("rftools:containeranditem", ContainerAndItemRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register("rftools:containertoitem", ContainerToItemRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register("rftools:shapedknowndimlet", KnownDimletShapedRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register("rftools:nbtmatchingrecipe", NBTMatchingRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
    }

    public static void init() {
        intBaseCrafting();
        initItemCrafting();

        MonitorSetup.setupCrafting();
        CrafterSetup.setupCrafting();
        MachineInfuserSetup.setupCrafting();
        StorageScannerSetup.setupCrafting();
        RelaySetup.setupCrafting();
        ItemFilterSetup.setupCrafting();
        TeleporterSetup.setupCrafting();
        LogicBlockSetup.setupCrafting();
        EndergenicSetup.setupCrafting();
        ShieldSetup.setupCrafting();
        DimletSetup.setupCrafting();
        EnvironmentalSetup.setupCrafting();
        SpawnerSetup.setupCrafting();
        ScreenSetup.setupCrafting();
        DimletConstructionSetup.setupCrafting();
        BlockProtectorSetup.setupCrafting();
        ModularStorageSetup.setupCrafting();
        SpaceProjectorSetup.setupCrafting();
        SecuritySetup.setupCrafting();
    }

    private static void initItemCrafting() {
        Item inkSac = (Item) Item.itemRegistry.getObjectById(351);
        ItemStack lapisStack = new ItemStack(Items.dye, 1, 4);

        GameRegistry.addRecipe(new ItemStack(ModItems.networkMonitorItem), "rlr", "iri", "rlr", 'r', Items.redstone, 'i', Items.iron_ingot, 'l', inkSac);

        GameRegistry.addRecipe(new ItemStack(ModItems.rfToolsManualItem), " r ", "rbr", " r ", 'r', Items.redstone, 'b', Items.book);
        GameRegistry.addRecipe(new ItemStack(ModItems.rfToolsManualDimensionItem), "r r", " b ", "r r", 'r', Items.redstone, 'b', Items.book);

        GameRegistry.addRecipe(new ItemStack(ModItems.smartWrenchItem), "  i", " l ", "l  ", 'i', Items.iron_ingot, 'l', lapisStack);

        GameRegistry.addRecipe(new ItemStack(ModItems.shapeCardItem, 1, ShapeCardItem.CARD_SHAPE), "pbp", "rir", "pbp", 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', Items.brick, 'p', Items.paper);

        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                new ItemStack(inkSac), new ItemStack(Blocks.obsidian), new ItemStack(inkSac),
                new ItemStack(Blocks.obsidian), new ItemStack(ModItems.shapeCardItem, 1, ShapeCardItem.CARD_SHAPE), new ItemStack(Blocks.obsidian),
                new ItemStack(inkSac), new ItemStack(Blocks.obsidian), new ItemStack(inkSac)
        }, new ItemStack(ModItems.shapeCardItem, 1, ShapeCardItem.CARD_VOID), 4));
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                new ItemStack(Items.redstone), new ItemStack(Items.diamond_pickaxe), new ItemStack(Items.redstone),
                new ItemStack(Items.iron_ingot), new ItemStack(ModItems.shapeCardItem, 1, ShapeCardItem.CARD_SHAPE), new ItemStack(Items.iron_ingot),
                new ItemStack(Items.redstone), new ItemStack(Items.diamond_shovel), new ItemStack(Items.redstone)
        }, new ItemStack(ModItems.shapeCardItem, 1, ShapeCardItem.CARD_QUARRY), 4));
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                new ItemStack(DimletSetup.dimensionalShard), new ItemStack(Items.nether_star), new ItemStack(DimletSetup.dimensionalShard),
                new ItemStack(Items.diamond), new ItemStack(ModItems.shapeCardItem, 1, ShapeCardItem.CARD_QUARRY), new ItemStack(Items.diamond),
                new ItemStack(DimletSetup.dimensionalShard), new ItemStack(Items.diamond), new ItemStack(DimletSetup.dimensionalShard)
        }, new ItemStack(ModItems.shapeCardItem, 1, ShapeCardItem.CARD_QUARRY_SILK), 4));
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                new ItemStack(DimletSetup.dimensionalShard), new ItemStack(Items.ghast_tear), new ItemStack(DimletSetup.dimensionalShard),
                new ItemStack(Items.emerald), new ItemStack(ModItems.shapeCardItem, 1, ShapeCardItem.CARD_QUARRY), new ItemStack(Items.diamond),
                new ItemStack(DimletSetup.dimensionalShard), new ItemStack(Items.redstone), new ItemStack(DimletSetup.dimensionalShard)
        }, new ItemStack(ModItems.shapeCardItem, 1, ShapeCardItem.CARD_QUARRY_FORTUNE), 4));
    }

    private static void intBaseCrafting() {
        ItemStack lapisStack = new ItemStack(Items.dye, 1, 4);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.machineFrame), "ili", "g g", "ili", 'i', Items.iron_ingot, 'g', Items.gold_nugget, 'l', lapisStack);

        GameRegistry.addRecipe(new ItemStack(ModBlocks.machineBase), "   ", "ggg", "sss", 'g', Items.gold_nugget, 's', Blocks.stone);
    }
}
