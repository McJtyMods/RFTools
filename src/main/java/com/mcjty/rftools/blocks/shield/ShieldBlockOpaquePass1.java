package com.mcjty.rftools.blocks.shield;

import net.minecraft.block.material.Material;

public class ShieldBlockOpaquePass1 extends AbstractShieldBlock {

    public ShieldBlockOpaquePass1(Material material) {
        super(material);
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
