package com.mcjty.rftools.blocks.shield;

public class ShieldBlockOpaquePass0NN extends AbstractShieldBlock {

    public ShieldBlockOpaquePass0NN() {
        super();
        setBlockName("shieldBlockOpaquePass0NN");
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
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
