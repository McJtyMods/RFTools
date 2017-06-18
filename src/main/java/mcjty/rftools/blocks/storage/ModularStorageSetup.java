package mcjty.rftools.blocks.storage;

import mcjty.rftools.items.storage.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

    }
}
