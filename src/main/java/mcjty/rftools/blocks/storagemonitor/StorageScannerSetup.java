package mcjty.rftools.blocks.storagemonitor;

import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class StorageScannerSetup {
    public static StorageScannerBlock storageScannerBlock;

    public static void init() {
        storageScannerBlock = new StorageScannerBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        storageScannerBlock.initModel();
    }

    public static void initCrafting() {
        Block redstoneTorch = Blocks.redstone_torch;
        GameRegistry.addRecipe(new ItemStack(storageScannerBlock), "ToT", "gMg", "ToT", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'o', Items.ender_pearl,
                'g', Items.gold_ingot);
    }
}
