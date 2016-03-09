package mcjty.rftools.blocks.shield;

public class ShieldTileEntity extends ShieldTEBase {

    public static final int MAX_SHIELD_SIZE = ShieldConfiguration.maxShieldSize;

    public ShieldTileEntity() {
        super(ShieldConfiguration.MAXENERGY, ShieldConfiguration.RECEIVEPERTICK);
        setSupportedBlocks(MAX_SHIELD_SIZE);
    }
}
