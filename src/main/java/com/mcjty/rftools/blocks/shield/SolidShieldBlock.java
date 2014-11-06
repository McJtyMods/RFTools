package com.mcjty.rftools.blocks.shield;

import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;

public class SolidShieldBlock extends AbstractShieldBlock {

    public SolidShieldBlock(Material material) {
        super(material);
        setBlockName("solidShieldBlock");
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        return blockShouldSideBeRendered(world, x, y, z, side);
    }


}
