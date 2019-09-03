package mcjty.rftools.blocks.shield;

import static mcjty.rftools.blocks.shield.ShieldSetup.TYPE_SHIELD_BLOCK2;

public class ShieldTileEntity2 extends ShieldTEBase {

    public ShieldTileEntity2() {
        super(TYPE_SHIELD_BLOCK2);
        setSupportedBlocks(ShieldConfiguration.maxShieldSize.get() * 4);
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
