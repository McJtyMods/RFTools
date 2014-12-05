package com.mcjty.rftools.blocks.shield;

public class ShieldBlockNOpaquePass1 extends AbstractShieldBlock {

    public ShieldBlockNOpaquePass1() {
        super();
        setBlockName("shieldBlockNOpaquePass1");
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderBlockPass() {
        return 1;
    }
}
