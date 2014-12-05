package com.mcjty.rftools.blocks.shield;

public class ShieldBlockNOpaquePass0 extends AbstractShieldBlock {

    public ShieldBlockNOpaquePass0() {
        super();
        setBlockName("shieldBlockNOpaquePass0");
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderBlockPass() {
        return 0;
    }
}
