package com.mcjty.rftools.blocks.shield;

import net.minecraft.block.material.Material;

public class ShieldBlockNOpaquePass0 extends AbstractShieldBlock {

    public ShieldBlockNOpaquePass0(Material material) {
        super(material);
        setBlockName("shieldBlockNOpaquePass0");
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
