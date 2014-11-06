package com.mcjty.rftools.blocks.shield;

import net.minecraft.block.material.Material;

public class ShieldBlockOpaquePass0NN extends AbstractShieldBlock {

    public ShieldBlockOpaquePass0NN(Material material) {
        super(material);
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
