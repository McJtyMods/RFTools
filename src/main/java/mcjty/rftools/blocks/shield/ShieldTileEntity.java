package mcjty.rftools.blocks.shield;

import static mcjty.rftools.blocks.shield.ShieldSetup.TYPE_SHIELD_BLOCK1;

public class ShieldTileEntity extends ShieldTEBase {

    public static final int MAX_SHIELD_SIZE = ShieldConfiguration.maxShieldSize.get();

    public ShieldTileEntity() {
        super(TYPE_SHIELD_BLOCK1);
        setSupportedBlocks(MAX_SHIELD_SIZE);
    }

    @Override
    protected int getConfigMaxEnergy() {
        return ShieldConfiguration.MAXENERGY.get();
    }

    @Override
    protected int getConfigRfPerTick() {
        return ShieldConfiguration.RECEIVEPERTICK.get();
    }
}
