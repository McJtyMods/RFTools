package mcjty.rftools.blocks.storage;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.container.GenericItemBlock;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.crafting.PreservingShapedRecipe;
import mcjty.rftools.items.storage.StorageModuleItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ModularStorageSetup {
    public static ModularStorageBlock modularStorageBlock;

    public static StorageModuleItem storageModuleItem;

    public static void setupBlocks() {
        modularStorageBlock = new ModularStorageBlock();
        GameRegistry.registerBlock(modularStorageBlock, GenericItemBlock.class, "modularStorageBlock");
        GameRegistry.registerTileEntity(ModularStorageTileEntity.class, "ModularStorageTileEntity");
    }

    public static void setupItems() {
        storageModuleItem = new StorageModuleItem();
        storageModuleItem.setUnlocalizedName("StorageModule");
        storageModuleItem.setCreativeTab(RFTools.tabRfTools);
        storageModuleItem.setTextureName(RFTools.MODID + ":storageModule");
        GameRegistry.registerItem(storageModuleItem, "storageModuleItem");
    }

    public static void setupCrafting() {
        GameRegistry.addRecipe(new ItemStack(modularStorageBlock), "rcr", "qMq", "rqr", 'M', ModBlocks.machineFrame, 'c', Blocks.chest, 'r', Items.redstone, 'q', Items.quartz);

        GameRegistry.addRecipe(new ItemStack(storageModuleItem, 1, 0), " c ", "gig", " r ", 'p', Items.paper, 'r', Items.redstone, 'i', Items.iron_ingot,
                'g', Items.gold_nugget, 'c', Blocks.chest);
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                null, new ItemStack(Blocks.chest), null,
                new ItemStack(Items.gold_ingot), new ItemStack(storageModuleItem, 1, 0), new ItemStack(Items.gold_ingot),
                null, new ItemStack(Items.redstone), null},
                new ItemStack(storageModuleItem, 1, 1), 4));
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                null, new ItemStack(Blocks.chest), null,
                new ItemStack(Blocks.gold_block), new ItemStack(storageModuleItem, 1, 1), new ItemStack(Blocks.gold_block),
                null, new ItemStack(Items.redstone), null},
                new ItemStack(storageModuleItem, 1, 2), 4));
    }
}
