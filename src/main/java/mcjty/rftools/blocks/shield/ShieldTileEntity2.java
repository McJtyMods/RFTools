package mcjty.rftools.blocks.shield;

public class ShieldTileEntity2 extends ShieldTEBase {

    public ShieldTileEntity2() {
        super(ShieldConfiguration.MAXENERGY.get(), ShieldConfiguration.RECEIVEPERTICK.get());
        setSupportedBlocks(ShieldConfiguration.maxShieldSize.get() * 4);
    }
}
