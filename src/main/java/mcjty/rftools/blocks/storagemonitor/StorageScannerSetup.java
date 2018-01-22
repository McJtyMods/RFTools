package mcjty.rftools.blocks.storagemonitor;

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
}
