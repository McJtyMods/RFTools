package mcjty.rftools.blocks.storage;

import mcjty.lib.container.GenericItemBlock;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.crafting.ContainerAndItemRecipe;
import mcjty.rftools.crafting.ContainerToItemRecipe;
import mcjty.rftools.crafting.PreservingShapedRecipe;
import mcjty.rftools.items.storage.*;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModularStorageSetup {
    public static ModularStorageBlock modularStorageBlock;
    public static RemoteStorageBlock remoteStorageBlock;

    public static StorageModuleTabletItem storageModuleTabletItem;

    public static StorageModuleItem storageModuleItem;
    public static OreDictTypeItem oreDictTypeItem;
    public static GenericTypeItem genericTypeItem;
    public static StorageFilterItem storageFilterItem;

    public static void init() {
        modularStorageBlock = new ModularStorageBlock();
        remoteStorageBlock = new RemoteStorageBlock();

        storageModuleTabletItem = new StorageModuleTabletItem();
        storageModuleItem = new StorageModuleItem();
        oreDictTypeItem = new OreDictTypeItem();
        genericTypeItem = new GenericTypeItem();
        storageFilterItem = new StorageFilterItem();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        modularStorageBlock.initModel();
        remoteStorageBlock.initModel();
        storageModuleTabletItem.initModel();
        storageModuleItem.initModel();
        oreDictTypeItem.initModel();
        genericTypeItem.initModel();
        storageFilterItem.initModel();
    }


    public static void initCrafting() {
        GameRegistry.addRecipe(new ItemStack(modularStorageBlock), "rcr", "qMq", "rqr", 'M', ModBlocks.machineFrame, 'c', Blocks.chest, 'r', Items.redstone, 'q', Items.quartz);
        GameRegistry.addRecipe(new ItemStack(remoteStorageBlock), "ece", "qMq", "eqe", 'M', ModBlocks.machineFrame, 'c', Blocks.chest, 'e', Items.ender_pearl, 'q', Items.quartz);

        GameRegistry.addRecipe(new ItemStack(oreDictTypeItem), " u ", "rir", " p ", 'u', Blocks.iron_ore, 'p', Items.paper, 'r', Items.redstone, 'i', Items.iron_ingot);
        GameRegistry.addRecipe(new ItemStack(genericTypeItem), " p ", "rir", " p ", 'p', Items.paper, 'r', Items.redstone, 'i', Items.iron_ingot);
        GameRegistry.addRecipe(new ItemStack(storageFilterItem), " h ", "rir", " p ", 'h', Blocks.hopper, 'p', Items.paper, 'r', Items.redstone, 'i', Items.iron_ingot);

        GameRegistry.addRecipe(new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_EMPTY), "geg", "rqr", "grg", 'g', Items.gold_nugget, 'e', Items.emerald, 'r', Blocks.redstone_block, 'q', Blocks.quartz_block);
        GameRegistry.addRecipe(new ContainerAndItemRecipe(new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_EMPTY), new ItemStack(storageModuleItem, 1, StorageModuleItem.STORAGE_TIER1),
                new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_FULL)));
        GameRegistry.addRecipe(new ContainerAndItemRecipe(new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_EMPTY), new ItemStack(storageModuleItem, 1, StorageModuleItem.STORAGE_TIER2),
                new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_FULL)));
        GameRegistry.addRecipe(new ContainerAndItemRecipe(new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_EMPTY), new ItemStack(storageModuleItem, 1, StorageModuleItem.STORAGE_TIER3),
                new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_FULL)));
        GameRegistry.addRecipe(new ContainerAndItemRecipe(new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_EMPTY), new ItemStack(storageModuleItem, 1, StorageModuleItem.STORAGE_REMOTE),
                new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_FULL)));
        GameRegistry.addRecipe(new ContainerToItemRecipe(new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_FULL),
                new ItemStack(storageModuleItem)));

        GameRegistry.addRecipe(new ItemStack(storageModuleItem, 1, StorageModuleItem.STORAGE_TIER1), " c ", "gig", "qrq", 'r', Items.redstone, 'i', Items.iron_ingot,
                'g', Items.gold_nugget, 'c', Blocks.chest, 'q', Items.quartz);
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                null, new ItemStack(Blocks.chest), null,
                new ItemStack(Items.gold_ingot), new ItemStack(storageModuleItem, 1, StorageModuleItem.STORAGE_TIER1), new ItemStack(Items.gold_ingot),
                new ItemStack(Items.quartz), new ItemStack(Items.redstone), new ItemStack(Items.quartz)},
                new ItemStack(storageModuleItem, 1, StorageModuleItem.STORAGE_TIER2), 4));
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                null, new ItemStack(Blocks.chest), null,
                new ItemStack(Blocks.gold_block), new ItemStack(storageModuleItem, 1, StorageModuleItem.STORAGE_TIER2), new ItemStack(Blocks.gold_block),
                new ItemStack(Blocks.quartz_block), new ItemStack(Blocks.redstone_block), new ItemStack(Blocks.quartz_block)},
                new ItemStack(storageModuleItem, 1, StorageModuleItem.STORAGE_TIER3), 4));
        GameRegistry.addRecipe(new ItemStack(storageModuleItem, 1, StorageModuleItem.STORAGE_REMOTE), "ece", "gig", "qrq", 'r', Items.redstone, 'i', Items.iron_ingot,
                'g', Items.gold_nugget, 'c', Blocks.chest, 'q', Items.quartz, 'e', Items.ender_pearl);
    }
}
