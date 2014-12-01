package com.mcjty.rftools.dimension.world.terrain;

import com.mcjty.rftools.dimension.world.GenericChunkProvider;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class FlatTerrainGenerator implements BaseTerrainGenerator {
    @Override
    public void generate(GenericChunkProvider provider, int chunkX, int chunkZ, Block[] aBlock) {
        byte waterLevel = 63;
        for (int x4 = 0; x4 < 4; ++x4) {
            for (int z4 = 0; z4 < 4; ++z4) {
                for (int height = 0; height < 256; ++height) {
                    for (int x = 0; x < 4; ++x) {
                        int index = ((x + (x4 * 4)) << 12) | ((0 + (z4 * 4)) << 8) | height;
                        short maxheight = 256;
                        index -= maxheight;

                        for (int z = 0; z < 4; ++z) {
                            if (height < waterLevel) {
                                aBlock[index += maxheight] = Blocks.stone;
                            } else {
                                aBlock[index += maxheight] = null;
                            }
                        }
                    }
                }
            }
        }

    }
}
