package mcjty.rftools.blocks.shield;

public class ShieldTileEntity extends ShieldTEBase {

    public static final int MAX_SHIELD_SIZE = ShieldConfiguration.maxShieldSize.get();

    public ShieldTileEntity() {
        super(ShieldConfiguration.MAXENERGY.get(), ShieldConfiguration.RECEIVEPERTICK.get());
        setSupportedBlocks(MAX_SHIELD_SIZE);
    }
}
