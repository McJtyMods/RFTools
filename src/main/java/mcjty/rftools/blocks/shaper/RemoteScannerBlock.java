package mcjty.rftools.blocks.shaper;

import net.minecraft.block.material.Material;

public class RemoteScannerBlock extends ScannerBlock {

    public RemoteScannerBlock() {
        super(RemoteScannerTileEntity.class, ScannerContainer.class, "remote_scanner");
    }
}
