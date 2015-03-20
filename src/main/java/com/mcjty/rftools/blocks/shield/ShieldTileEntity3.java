package com.mcjty.rftools.blocks.shield;

public class ShieldTileEntity3 extends ShieldTEBase {

    public ShieldTileEntity3() {
        super();
        setSupportedBlocks(ShieldConfiguration.maxShieldSize * 4);
        setDamageFactor(5.0f);
    }
}
