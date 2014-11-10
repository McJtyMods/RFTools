package com.mcjty.rftools.blocks.shield;

import net.minecraft.block.material.Material;

public class ShieldBlockNOpaquePass1 extends AbstractShieldBlock {

    public ShieldBlockNOpaquePass1(Material material) {
        super(material);
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
