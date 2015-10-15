package mcjty.rftools.blocks.shield;

public class ShieldTileEntity3 extends ShieldTEBase {

    public static final int MAX_SHIELD_SIZE = ShieldConfiguration.maxShieldSize * 16;

    public ShieldTileEntity3() {
        super(ShieldConfiguration.MAXENERGY * 3, ShieldConfiguration.RECEIVEPERTICK * 2);
        setSupportedBlocks(MAX_SHIELD_SIZE);
        setDamageFactor(4.0f);
        setCostFactor(2.0f);
    }
}
