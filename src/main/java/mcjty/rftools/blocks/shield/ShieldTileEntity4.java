package mcjty.rftools.blocks.shield;

public class ShieldTileEntity4 extends ShieldTEBase {

    public ShieldTileEntity4() {
        super(ShieldConfiguration.MAXENERGY.get() * 6, ShieldConfiguration.RECEIVEPERTICK.get() * 6);
        setSupportedBlocks(ShieldConfiguration.maxShieldSize.get() * 128);
        setDamageFactor(4.0f);
        setCostFactor(2.0f);
    }
}
