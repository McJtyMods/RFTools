package mcjty.rftools.blocks.shield;

public class ShieldTileEntity2 extends ShieldTEBase {

    public static final int MAX_SHIELD_SIZE = ShieldConfiguration.maxShieldSize * 4;

    public ShieldTileEntity2() {
        super(ShieldConfiguration.MAXENERGY, ShieldConfiguration.RECEIVEPERTICK);
        setSupportedBlocks(MAX_SHIELD_SIZE);
    }
}
