package mcjty.rftools.blocks.storage;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.container.GenericItemBlock;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.crafting.PreservingShapedRecipe;
import mcjty.rftools.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ModularStorageSetup {
    public static ModularStorageBlock modularStorageBlock;

    public static void setupBlocks() {
//        modularStorageBlock = new ModularStorageBlock();
//        GameRegistry.registerBlock(modularStorageBlock, GenericItemBlock.class, "modularStorageBlock");
//        GameRegistry.registerTileEntity(ModularStorageTileEntity.class, "ModularStorageTileEntity");
    }

    public static void setupCrafting() {
//        GameRegistry.addRecipe(new ItemStack(modularStorageBlock), "rcr", "qMq", "rqr", 'M', ModBlocks.machineFrame, 'c', Blocks.chest, 'r', Items.redstone, 'q', Items.quartz);
//
//        GameRegistry.addRecipe(new ItemStack(ModItems.storageModuleItem, 1, 0), " c ", "gig", " r ", 'p', Items.paper, 'r', Items.redstone, 'i', Items.iron_ingot,
//                'g', Items.gold_nugget, 'c', Blocks.chest);
//        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
//                null, new ItemStack(Blocks.chest), null,
//                new ItemStack(Items.gold_ingot), new ItemStack(ModItems.storageModuleItem, 1, 0), new ItemStack(Items.gold_ingot),
//                null, new ItemStack(Items.redstone), null},
//                new ItemStack(ModItems.storageModuleItem, 1, 1), 4));
//        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
//                null, new ItemStack(Blocks.chest), null,
//                new ItemStack(Blocks.gold_block), new ItemStack(ModItems.storageModuleItem, 1, 1), new ItemStack(Blocks.gold_block),
//                null, new ItemStack(Items.redstone), null},
//                new ItemStack(ModItems.storageModuleItem, 1, 2), 4));
    }
}
