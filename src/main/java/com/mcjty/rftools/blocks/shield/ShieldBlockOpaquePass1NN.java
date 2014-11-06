package com.mcjty.rftools.blocks.shield;

import net.minecraft.block.material.Material;

public class ShieldBlockOpaquePass1NN extends AbstractShieldBlock {

    public ShieldBlockOpaquePass1NN(Material material) {
        super(material);
        setBlockName("shieldBlockOpaquePass1NN");
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
        return 1;
    }
}
