package mcjty.rftools.blocks.shield;

public class ShieldTileEntity3 extends ShieldTEBase {

    public ShieldTileEntity3() {
        super(ShieldConfiguration.MAXENERGY.get() * 3, ShieldConfiguration.RECEIVEPERTICK.get() * 2);
        setSupportedBlocks(ShieldConfiguration.maxShieldSize.get() * 16);
        setDamageFactor(4.0f);
        setCostFactor(2.0f);
    }
}
