package com.mcjty.rftools.blocks.shield;

import net.minecraft.block.material.Material;

public class VisibleShieldBlock extends AbstractShieldBlock {

    public VisibleShieldBlock(Material material) {
        super(material);
        setBlockName("visibleShieldBlock");
    }

    @Override
    public int getRenderBlockPass() {
        return 1;
    }
}
