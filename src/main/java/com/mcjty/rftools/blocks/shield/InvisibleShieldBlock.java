package com.mcjty.rftools.blocks.shield;

import net.minecraft.block.material.Material;

public class InvisibleShieldBlock extends AbstractShieldBlock {

    public InvisibleShieldBlock(Material material) {
        super(material);
        setBlockName("invisibleShieldBlock");
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return -1;              // Invisible
    }
}
