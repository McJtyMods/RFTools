package mcjty.rftools.blocks.storagemonitor;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.lib.container.GenericItemBlock;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class StorageScannerSetup {
    public static StorageScannerBlock storageScannerBlock;

    public static void setupBlocks() {
        storageScannerBlock = new StorageScannerBlock();
        GameRegistry.registerBlock(storageScannerBlock, GenericItemBlock.class, "storageScannerBlock");
        GameRegistry.registerTileEntity(StorageScannerTileEntity.class, "StorageScannerTileEntity");
    }

    public static void setupCrafting() {
        Object redstoneTorch = Item.itemRegistry.getObject("redstone_torch");
        GameRegistry.addRecipe(new ItemStack(storageScannerBlock), "ToT", "gMg", "ToT", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'o', Items.ender_pearl,
                'g', Items.gold_ingot);
    }
}
