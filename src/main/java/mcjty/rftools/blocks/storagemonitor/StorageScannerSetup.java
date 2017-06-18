package mcjty.rftools.blocks.storagemonitor;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
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
        Block redstoneTorch = Blocks.REDSTONE_TORCH;

    }
}
