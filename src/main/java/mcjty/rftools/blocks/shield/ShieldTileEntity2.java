package mcjty.rftools.blocks.shield;

public class ShieldTileEntity2 extends ShieldTEBase {

    public ShieldTileEntity2() {
        super(ShieldConfiguration.MAXENERGY, ShieldConfiguration.RECEIVEPERTICK);
        setSupportedBlocks(ShieldConfiguration.maxShieldSize * 4);
    }
}
