package mcjty.rftools.blocks.storage;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.container.GenericItemBlock;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.dimlets.DimletSetup;
import mcjty.rftools.crafting.PreservingShapedRecipe;
import mcjty.rftools.items.storage.DimletTypeItem;
import mcjty.rftools.items.storage.GenericTypeItem;
import mcjty.rftools.items.storage.OreDictTypeItem;
import mcjty.rftools.items.storage.StorageModuleItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ModularStorageSetup {
    public static ModularStorageBlock modularStorageBlock;
    public static RemoteStorageBlock remoteStorageBlock;

    public static StorageModuleItem storageModuleItem;
    public static DimletTypeItem dimletTypeItem;
    public static OreDictTypeItem oreDictTypeItem;
    public static GenericTypeItem genericTypeItem;

    public static void setupBlocks() {
        modularStorageBlock = new ModularStorageBlock();
        GameRegistry.registerBlock(modularStorageBlock, GenericItemBlock.class, "modularStorageBlock");
        GameRegistry.registerTileEntity(ModularStorageTileEntity.class, "ModularStorageTileEntity");

        remoteStorageBlock = new RemoteStorageBlock();
        GameRegistry.registerBlock(remoteStorageBlock, GenericItemBlock.class, "remoteStorageBlock");
        GameRegistry.registerTileEntity(RemoteStorageTileEntity.class, "RemoteStorageTileEntity");
    }

    public static void setupItems() {
        storageModuleItem = new StorageModuleItem();
        storageModuleItem.setUnlocalizedName("StorageModule");
        storageModuleItem.setCreativeTab(RFTools.tabRfTools);
        storageModuleItem.setTextureName(RFTools.MODID + ":storage/storageModule");
        GameRegistry.registerItem(storageModuleItem, "storageModuleItem");

        dimletTypeItem = new DimletTypeItem();
        dimletTypeItem.setUnlocalizedName("DimletModule");
        dimletTypeItem.setCreativeTab(RFTools.tabRfTools);
        dimletTypeItem.setTextureName(RFTools.MODID + ":storage/dimletModule");
        GameRegistry.registerItem(dimletTypeItem, "dimletTypeItem");

        oreDictTypeItem = new OreDictTypeItem();
        oreDictTypeItem.setUnlocalizedName("OreDictModule");
        oreDictTypeItem.setCreativeTab(RFTools.tabRfTools);
        oreDictTypeItem.setTextureName(RFTools.MODID + ":storage/oreDictModule");
        GameRegistry.registerItem(oreDictTypeItem, "oreDictTypeItem");

        genericTypeItem = new GenericTypeItem();
        genericTypeItem.setUnlocalizedName("GenericModule");
        genericTypeItem.setCreativeTab(RFTools.tabRfTools);
        genericTypeItem.setTextureName(RFTools.MODID + ":storage/genericModule");
        GameRegistry.registerItem(genericTypeItem, "genericTypeItem");
    }

    public static void setupCrafting() {
        GameRegistry.addRecipe(new ItemStack(modularStorageBlock), "rcr", "qMq", "rqr", 'M', ModBlocks.machineFrame, 'c', Blocks.chest, 'r', Items.redstone, 'q', Items.quartz);
        GameRegistry.addRecipe(new ItemStack(remoteStorageBlock), "ece", "qMq", "eqe", 'M', ModBlocks.machineFrame, 'c', Blocks.chest, 'e', Items.ender_pearl, 'q', Items.quartz);

        GameRegistry.addRecipe(new ItemStack(dimletTypeItem), " u ", "rir", " p ", 'u', DimletSetup.unknownDimlet, 'p', Items.paper, 'r', Items.redstone, 'i', Items.iron_ingot);
        GameRegistry.addRecipe(new ItemStack(oreDictTypeItem), " u ", "rir", " p ", 'u', Blocks.iron_ore, 'p', Items.paper, 'r', Items.redstone, 'i', Items.iron_ingot);
        GameRegistry.addRecipe(new ItemStack(genericTypeItem), " p ", "rir", " p ", 'p', Items.paper, 'r', Items.redstone, 'i', Items.iron_ingot);

        GameRegistry.addRecipe(new ItemStack(storageModuleItem, 1, 0), " c ", "gig", "qrq", 'r', Items.redstone, 'i', Items.iron_ingot,
                'g', Items.gold_nugget, 'c', Blocks.chest, 'q', Items.quartz);
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                null, new ItemStack(Blocks.chest), null,
                new ItemStack(Items.gold_ingot), new ItemStack(storageModuleItem, 1, 0), new ItemStack(Items.gold_ingot),
                new ItemStack(Items.quartz), new ItemStack(Items.redstone), new ItemStack(Items.quartz)},
                new ItemStack(storageModuleItem, 1, 1), 4));
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                null, new ItemStack(Blocks.chest), null,
                new ItemStack(Blocks.gold_block), new ItemStack(storageModuleItem, 1, 1), new ItemStack(Blocks.gold_block),
                new ItemStack(Blocks.quartz_block), new ItemStack(Blocks.redstone_block), new ItemStack(Blocks.quartz_block)},
                new ItemStack(storageModuleItem, 1, 2), 4));
    }
}
