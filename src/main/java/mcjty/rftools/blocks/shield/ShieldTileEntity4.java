package mcjty.rftools.blocks.shield;

public class ShieldTileEntity4 extends ShieldTEBase {

    public static final int MAX_SHIELD_SIZE = ShieldConfiguration.maxShieldSize * 128;

    public ShieldTileEntity4() {
        super(ShieldConfiguration.MAXENERGY * 6, ShieldConfiguration.RECEIVEPERTICK * 6);
        setSupportedBlocks(MAX_SHIELD_SIZE);
        setDamageFactor(4.0f);
        setCostFactor(2.0f);
    }
}
