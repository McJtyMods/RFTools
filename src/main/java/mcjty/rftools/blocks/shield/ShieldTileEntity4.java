package mcjty.rftools.blocks.shield;

public class ShieldTileEntity4 extends ShieldTEBase {

    public ShieldTileEntity4() {
        super(ShieldConfiguration.MAXENERGY * 6, ShieldConfiguration.RECEIVEPERTICK * 6);
        setSupportedBlocks(ShieldConfiguration.maxShieldSize * 128);
        setDamageFactor(4.0f);
        setCostFactor(2.0f);
    }
}
