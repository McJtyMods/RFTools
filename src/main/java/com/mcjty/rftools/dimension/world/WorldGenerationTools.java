package com.mcjty.rftools.dimension.world;

import net.minecraft.world.World;

public class WorldGenerationTools {

    public static int findSuitableEmptySpot(World world, int x, int z, int minheight, int tries) {
        int y = world.getTopSolidOrLiquidBlock(x, z);
        if (y == -1) {
            return -1;
        }
        return y-1;
    }
}
