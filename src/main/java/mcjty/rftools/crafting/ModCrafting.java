package mcjty.rftools.crafting;

import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.crafter.CrafterSetup;
import mcjty.rftools.blocks.generator.CoalGeneratorSetup;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
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
        intBaseCrafting();
        initItemCrafting();

        CoalGeneratorSetup.initCrafting();
        CrafterSetup.initCrafting();
    }

    private static void initItemCrafting() {
    }

    private static void intBaseCrafting() {
        ItemStack lapisStack = new ItemStack(Items.dye, 1, 4);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.machineFrame), "ili", "g g", "ili", 'i', Items.iron_ingot, 'g', Items.gold_nugget, 'l', lapisStack);

        GameRegistry.addRecipe(new ItemStack(ModBlocks.machineBase), "   ", "ggg", "sss", 'g', Items.gold_nugget, 's', Blocks.stone);
    }
}
