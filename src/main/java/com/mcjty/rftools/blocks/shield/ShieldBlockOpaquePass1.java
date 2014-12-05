package com.mcjty.rftools.blocks.shield;

public class ShieldBlockOpaquePass1 extends AbstractShieldBlock {

    public ShieldBlockOpaquePass1() {
        super();
        setBlockName("shieldBlockOpaquePass1");
    }

    @Override
    public boolean isOpaqueCube() {
        return true;
    }

    @Override
    public int getRenderBlockPass() {
        return 1;
    }
}
