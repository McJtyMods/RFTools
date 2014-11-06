package com.mcjty.rftools.blocks.shield;

import net.minecraft.block.material.Material;

public class ShieldBlockNOpaquePass0NN extends AbstractShieldBlock {

    public ShieldBlockNOpaquePass0NN(Material material) {
        super(material);
        setBlockName("shieldBlockNOpaquePass0NN");
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
        return 0;
    }
}
