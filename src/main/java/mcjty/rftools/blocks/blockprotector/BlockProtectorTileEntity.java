package mcjty.rftools.blocks.blockprotector;

import mcjty.entity.GenericEnergyReceiverTileEntity;

public class BlockProtectorTileEntity extends GenericEnergyReceiverTileEntity {

    public BlockProtectorTileEntity() {
        super(BlockProtectorConfiguration.MAXENERGY, BlockProtectorConfiguration.RECEIVEPERTICK);
    }


}
