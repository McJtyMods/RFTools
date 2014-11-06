package com.mcjty.rftools.blocks.shield;

import net.minecraft.block.material.Material;

public class ShieldBlockOpaquePass0 extends AbstractShieldBlock {

    public ShieldBlockOpaquePass0(Material material) {
        super(material);
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
