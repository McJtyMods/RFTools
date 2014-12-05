package com.mcjty.rftools.blocks.shield;

public class ShieldBlockOpaquePass0 extends AbstractShieldBlock {

    public ShieldBlockOpaquePass0() {
        super();
        setBlockName("shieldBlockOpaquePass0");
    }

    @Override
    public boolean isOpaqueCube() {
        return true;
    }

    @Override
    public int getRenderBlockPass() {
        return 0;
    }
}
