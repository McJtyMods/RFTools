package com.mcjty.rftools.dimension.world.terrain;

import com.mcjty.rftools.dimension.world.types.FeatureType;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import java.util.Random;

public class FlatTerrainGenerator extends NormalTerrainGenerator {

    @Override
    public void generate(int chunkX, int chunkZ, Block[] aBlock) {
        Block baseBlock = provider.dimensionInformation.getBaseBlockForTerrain();

        byte waterLevel = 63;

        boolean elevated = false;
        if (provider.dimensionInformation.hasFeatureType(FeatureType.FEATURE_MAZE)) {
            long s2 = ((chunkX + provider.seed + 13) * 314) + chunkZ * 17L;
            Random rand = new Random(s2);
            rand.nextFloat();   // Skip one.
            elevated = (chunkX & 1) == 0;
            if (rand.nextFloat() < .2f) {
                elevated = !elevated;
            }
            if (elevated) {
                waterLevel = 120;
            } else {
                waterLevel = 40;
            }
        }


        for (int x4 = 0; x4 < 4; ++x4) {
            for (int z4 = 0; z4 < 4; ++z4) {
                for (int height = 0; height < 256; ++height) {
                    for (int x = 0; x < 4; ++x) {
                        int index = ((x + (x4 * 4)) << 12) | ((0 + (z4 * 4)) << 8) | height;
                        short maxheight = 256;
                        index -= maxheight;

                        for (int z = 0; z < 4; ++z) {
                            if (height < waterLevel) {
                                aBlock[index += maxheight] = baseBlock;
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
