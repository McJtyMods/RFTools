package mcjty.rftools.blocks.shield;

public class ShieldTileEntity3 extends ShieldTEBase {

    public ShieldTileEntity3() {
        super(ShieldConfiguration.MAXENERGY * 4, ShieldConfiguration.RECEIVEPERTICK * 4);
        setSupportedBlocks(ShieldConfiguration.maxShieldSize * 32);
        setDamageFactor(4.0f);
        setCostFactor(2.0f);
    }
}
