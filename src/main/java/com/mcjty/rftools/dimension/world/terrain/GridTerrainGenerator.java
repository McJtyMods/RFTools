package com.mcjty.rftools.dimension.world.terrain;

import net.minecraft.block.Block;
import net.minecraft.world.biome.BiomeGenBase;

public class GridTerrainGenerator extends NormalTerrainGenerator {

    @Override
    public void generate(int chunkX, int chunkZ, Block[] aBlock, byte[] abyte) {
        Block baseBlock = provider.dimensionInformation.getBaseBlockForTerrain().getBlock();
        byte baseMeta = provider.dimensionInformation.getBaseBlockForTerrain().getMeta();

        int borderx;
        if ((chunkX & 1) == 0) {
            borderx = 0;
        } else {
            borderx = 15;
        }
        int borderz;
        if ((chunkZ & 1) == 0) {
            borderz = 0;
        } else {
            borderz = 15;
        }

        int index = 0;
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                // Clear the bedrock
                for (int y = 0 ; y < 10 ; y++) {
                    aBlock[index+y] = null;
                }

                boolean filled = (x == borderx) && (z == borderz);
                if (filled) {
                    for (int y = 0 ; y < 128 ; y++) {
                        aBlock[index] = baseBlock;
                        abyte[index++] = baseMeta;
                    }
                    index += 128;
                } else if (x == borderx || z == borderz) {
                    for (int y = 0 ; y < 128 ; y+=32) {
                        if (y > 0) {
                            aBlock[index-1] = baseBlock;
                            abyte[index-1] = baseMeta;
                        }
                        aBlock[index] = baseBlock;
                        abyte[index] = baseMeta;
                        index += 32;
                    }
                    index += 128;
                } else {
                    index += 256;
                }
            }
        }
    }

    @Override
    public void replaceBlocksForBiome(int chunkX, int chunkZ, Block[] aBlock, byte[] abyte, BiomeGenBase[] biomeGenBases) {
    }
}
