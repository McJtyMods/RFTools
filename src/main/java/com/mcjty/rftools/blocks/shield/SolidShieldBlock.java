package com.mcjty.rftools.blocks.shield;

import net.minecraft.world.IBlockAccess;

public class SolidShieldBlock extends AbstractShieldBlock {

    public SolidShieldBlock() {
        super();
        setBlockName("solidShieldBlock");
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        return blockShouldSideBeRendered(world, x, y, z, side);
    }


}
