package mcjty.rftools.crafting;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.rftools.blocks.ModBlocks;
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
import mcjty.rftools.blocks.shield.ShieldSetup;
import mcjty.rftools.blocks.spawner.SpawnerSetup;
import mcjty.rftools.blocks.storagemonitor.StorageScannerSetup;
import mcjty.rftools.blocks.teleporter.TeleporterSetup;
import mcjty.rftools.items.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.RecipeSorter;

public final class ModCrafting {
    static {
        RecipeSorter.register("rftools:shapedpreserving", PreservingShapedRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
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
    }

    private static void initItemCrafting() {
        Object inkSac = Item.itemRegistry.getObjectById(351);
        GameRegistry.addRecipe(new ItemStack(ModItems.networkMonitorItem), "rlr", "iri", "rlr", 'r', Items.redstone, 'i', Items.iron_ingot, 'l', inkSac);

        GameRegistry.addRecipe(new ItemStack(ModItems.rfToolsManualItem), " r ", "rbr", " r ", 'r', Items.redstone, 'b', Items.book);
        GameRegistry.addRecipe(new ItemStack(ModItems.rfToolsManualDimensionItem), "r r", " b ", "r r", 'r', Items.redstone, 'b', Items.book);
    }

    private static void intBaseCrafting() {
        ItemStack lapisStack = new ItemStack(Items.dye, 1, 4);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.machineFrame), "ili", "g g", "ili", 'i', Items.iron_ingot, 'g', Items.gold_nugget, 'l', lapisStack);

        GameRegistry.addRecipe(new ItemStack(ModBlocks.machineBase), "   ", "ggg", "sss", 'g', Items.gold_nugget, 's', Blocks.stone);
    }
}
