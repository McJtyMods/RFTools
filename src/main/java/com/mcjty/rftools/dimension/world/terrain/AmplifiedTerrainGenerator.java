package com.mcjty.rftools.dimension.world.terrain;

import com.mcjty.rftools.dimension.world.GenericChunkProvider;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class AmplifiedTerrainGenerator implements BaseTerrainGenerator {
    @Override
    public void generate(GenericChunkProvider provider, int chunkX, int chunkZ, Block[] aBlock) {
        // @todo Make actually amplified!
        byte waterLevel = 63;
        for (int x4 = 0; x4 < 4; ++x4) {
            int l = x4 * 5;
            int i1 = (x4 + 1) * 5;

            for (int z4 = 0; z4 < 4; ++z4) {
                int k1 = (l + z4) * 33;
                int l1 = (l + z4 + 1) * 33;
                int i2 = (i1 + z4) * 33;
                int j2 = (i1 + z4 + 1) * 33;

                for (int height32 = 0; height32 < 32; ++height32) {
                    double d0 = 0.125D;
                    double d1 = provider.field_147434_q[k1 + height32];
                    double d2 = provider.field_147434_q[l1 + height32];
                    double d3 = provider.field_147434_q[i2 + height32];
                    double d4 = provider.field_147434_q[j2 + height32];
                    double d5 = (provider.field_147434_q[k1 + height32 + 1] - d1) * d0;
                    double d6 = (provider.field_147434_q[l1 + height32 + 1] - d2) * d0;
                    double d7 = (provider.field_147434_q[i2 + height32 + 1] - d3) * d0;
                    double d8 = (provider.field_147434_q[j2 + height32 + 1] - d4) * d0;

                    for (int h = 0; h < 8; ++h) {
                        double d9 = 0.25D;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * d9;
                        double d13 = (d4 - d2) * d9;

                        for (int x = 0; x < 4; ++x) {
                            int height = (height32 * 8) + h;
                            int index = ((x + (x4 * 4)) << 12) | ((0 + (z4 * 4)) << 8) | height;
                            short maxheight = 256;
                            index -= maxheight;
                            double d14 = 0.25D;
                            double d16 = (d11 - d10) * d14;
                            double d15 = d10 - d16;

                            for (int z = 0; z < 4; ++z) {
                                if ((d15 += d16) > 0.0D) {
                                    aBlock[index += maxheight] = Blocks.stone;
                                } else if (height < waterLevel) {
                                    aBlock[index += maxheight] = Blocks.water;
                                } else {
                                    aBlock[index += maxheight] = null;
                                }
                            }

                            d10 += d12;
                            d11 += d13;
                        }

                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }
    }
}
