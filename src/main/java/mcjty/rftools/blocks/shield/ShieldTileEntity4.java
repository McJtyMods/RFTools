package mcjty.rftools.blocks.shield;

import static mcjty.rftools.blocks.shield.ShieldSetup.TYPE_SHIELD_BLOCK4;

public class ShieldTileEntity4 extends ShieldTEBase {

    public ShieldTileEntity4() {
        super(TYPE_SHIELD_BLOCK4);
        setSupportedBlocks(ShieldConfiguration.maxShieldSize.get() * 128);
        setDamageFactor(4.0f);
        setCostFactor(2.0f);
    }

    @Override
    protected int getConfigMaxEnergy() {
        return ShieldConfiguration.MAXENERGY.get() * 6;
    }

    @Override
    protected int getConfigRfPerTick() {
        return ShieldConfiguration.RECEIVEPERTICK.get() * 6;
    }
}
