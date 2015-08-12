package mcjty.rftools.blocks.shield;

public class ShieldTileEntity extends ShieldTEBase {

    public ShieldTileEntity() {
        super(ShieldConfiguration.MAXENERGY, ShieldConfiguration.RECEIVEPERTICK);
        setSupportedBlocks(ShieldConfiguration.maxShieldSize);
    }
}
