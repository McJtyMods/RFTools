package mcjty.rftools.blocks.storage;

import mcjty.lib.compat.MyGameReg;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.screens.ScreenSetup;
import mcjty.rftools.crafting.ContainerAndItemRecipe;
import mcjty.rftools.crafting.ContainerToItemRecipe;
import mcjty.rftools.crafting.PreservingShapedOreRecipe;
import mcjty.rftools.items.storage.*;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;

import static mcjty.rftools.items.storage.StorageModuleTabletItem.META_FOR_SCANNER;

public class ModularStorageSetup {
    public static ModularStorageBlock modularStorageBlock;
    public static RemoteStorageBlock remoteStorageBlock;
    public static StorageTerminalBlock storageTerminalBlock;
    public static LevelEmitterBlock levelEmitterBlock;

    public static StorageModuleTabletItem storageModuleTabletItem;

    public static StorageModuleItem storageModuleItem;
    public static OreDictTypeItem oreDictTypeItem;
    public static GenericTypeItem genericTypeItem;
    public static StorageFilterItem storageFilterItem;

    public static void init() {
        modularStorageBlock = new ModularStorageBlock();
        remoteStorageBlock = new RemoteStorageBlock();
        storageTerminalBlock = new StorageTerminalBlock();
        levelEmitterBlock = new LevelEmitterBlock();

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
        storageTerminalBlock.initModel();
        levelEmitterBlock.initModel();
        storageModuleTabletItem.initModel();
        storageModuleItem.initModel();
        oreDictTypeItem.initModel();
        genericTypeItem.initModel();
        storageFilterItem.initModel();
    }


    public static void initCrafting() {
        MyGameReg.addRecipe(new ItemStack(modularStorageBlock), "rcr", "qMq", "rqr", 'M', ModBlocks.machineFrame, 'c', "chest", 'r', Items.REDSTONE, 'q', Items.QUARTZ);
        MyGameReg.addRecipe(new ItemStack(remoteStorageBlock), "ece", "qMq", "eqe", 'M', ModBlocks.machineFrame, 'c', "chest", 'e', Items.ENDER_PEARL, 'q', Items.QUARTZ);
        MyGameReg.addRecipe(new ItemStack(storageTerminalBlock), "pge", "gMg", "egp", 'M', ModBlocks.machineBase, 'g', Blocks.GLASS, 'e', Items.ENDER_PEARL, 'p', Items.PAPER);
        MyGameReg.addRecipe(new ItemStack(levelEmitterBlock), "pce", "gMg", "egp", 'M', ModBlocks.machineBase, 'g', Blocks.GLASS, 'e', Items.ENDER_PEARL, 'p', Items.PAPER, 'c', Items.COMPARATOR);

        MyGameReg.addRecipe(new ItemStack(oreDictTypeItem), " u ", "rir", " p ", 'u', Blocks.IRON_ORE, 'p', Items.PAPER, 'r', Items.REDSTONE, 'i', Items.IRON_INGOT);
        MyGameReg.addRecipe(new ItemStack(genericTypeItem), " p ", "rir", " p ", 'p', Items.PAPER, 'r', Items.REDSTONE, 'i', Items.IRON_INGOT);
        MyGameReg.addRecipe(new ItemStack(storageFilterItem), " h ", "rir", " p ", 'h', Blocks.HOPPER, 'p', Items.PAPER, 'r', Items.REDSTONE, 'i', Items.IRON_INGOT);

        MyGameReg.addRecipe(new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_EMPTY), "geg", "rqr", "grg", 'g', Items.GOLD_NUGGET, 'e', Items.EMERALD, 'r', Blocks.REDSTONE_BLOCK, 'q', Blocks.QUARTZ_BLOCK);
        // @todo recipes
//        MyGameReg.addRecipe(new ContainerAndItemRecipe(new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_EMPTY), new ItemStack(storageModuleItem, 1, StorageModuleItem.STORAGE_TIER1),
//                new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_FULL), null));
//        MyGameReg.addRecipe(new ContainerAndItemRecipe(new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_EMPTY), new ItemStack(storageModuleItem, 1, StorageModuleItem.STORAGE_TIER2),
//                new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_FULL), null));
//        MyGameReg.addRecipe(new ContainerAndItemRecipe(new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_EMPTY), new ItemStack(storageModuleItem, 1, StorageModuleItem.STORAGE_TIER3),
//                new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_FULL), null));
//        MyGameReg.addRecipe(new ContainerAndItemRecipe(new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_EMPTY), new ItemStack(storageModuleItem, 1, StorageModuleItem.STORAGE_REMOTE),
//                new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_FULL), null));
//        MyGameReg.addRecipe(new ContainerAndItemRecipe(new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_EMPTY), new ItemStack(ScreenSetup.storageControlModuleItem, 1),
//                new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_SCANNER), i -> META_FOR_SCANNER));
//        MyGameReg.addRecipe(new ContainerToItemRecipe(new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_FULL),
//                new ItemStack(storageModuleItem), null));
//        MyGameReg.addRecipe(new ContainerToItemRecipe(new ItemStack(storageModuleTabletItem, 1, StorageModuleTabletItem.DAMAGE_SCANNER),
//                new ItemStack(ScreenSetup.storageControlModuleItem), n -> 0));

        MyGameReg.addRecipe(new ItemStack(storageModuleItem, 1, StorageModuleItem.STORAGE_TIER1), " c ", "gig", "qrq", 'r', Items.REDSTONE, 'i', Items.IRON_INGOT,
                'g', Items.GOLD_NUGGET, 'c', "chest", 'q', Items.QUARTZ);
        // @todo recipes
//        MyGameReg.addRecipe(new PreservingShapedOreRecipe(
//                new ItemStack(storageModuleItem, 1, StorageModuleItem.STORAGE_TIER2), 4,
//                " c ", "gmg", "qrq",
//                'c', "chest", 'g', "ingotGold", 'r', Items.REDSTONE, 'q', Items.QUARTZ,
//                'm', new ItemStack(storageModuleItem, 1, StorageModuleItem.STORAGE_TIER1)
//                ));
//        MyGameReg.addRecipe(new PreservingShapedOreRecipe(
//                new ItemStack(storageModuleItem, 1, StorageModuleItem.STORAGE_TIER3), 4,
//                " c ", "gmg", "qrq",
//                'c', "chest", 'g', "blockGold", 'r', Blocks.REDSTONE_BLOCK, 'q', Blocks.QUARTZ_BLOCK,
//                'm', new ItemStack(storageModuleItem, 1, StorageModuleItem.STORAGE_TIER2)
//        ));
        MyGameReg.addRecipe(new ItemStack(storageModuleItem, 1, StorageModuleItem.STORAGE_REMOTE), "ece", "gig", "qrq", 'r', Items.REDSTONE, 'i', Items.IRON_INGOT,
                'g', Items.GOLD_NUGGET, 'c', "chest", 'q', Items.QUARTZ, 'e', Items.ENDER_PEARL);
    }
}
