package com.mcjty.rftools.blocks.shield;

public class ShieldBlockNOpaquePass1NN extends AbstractShieldBlock {

    public ShieldBlockNOpaquePass1NN() {
        super();
        setBlockName("shieldBlockNOpaquePass1NN");
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
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
