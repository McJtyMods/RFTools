package com.mcjty.rftools.dimension.world.terrain;

import com.mcjty.rftools.dimension.world.GenericChunkProvider;
import com.mcjty.rftools.dimension.world.types.FeatureType;
import com.mcjty.rftools.items.dimlets.BlockMeta;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.block.Block;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.NoiseGenerator;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.ChunkProviderEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import java.util.Random;

public class NormalTerrainGenerator implements BaseTerrainGenerator {
    private World world;
    protected GenericChunkProvider provider;

    private final double[] noiseField;
    private double[] noiseData1;
    private double[] noiseData2;
    private double[] noiseData3;
    private double[] noiseData4;

    private NoiseGeneratorOctaves noiseGen1;
    private NoiseGeneratorOctaves noiseGen2;
    private NoiseGeneratorOctaves noiseGen3;
    private NoiseGeneratorPerlin noiseGen4;

    // A NoiseGeneratorOctaves used in generating terrain
    private NoiseGeneratorOctaves noiseGen6;

    private final float[] parabolicField;
    private double[] stoneNoise = new double[256];


    public NormalTerrainGenerator() {
        this.noiseField = new double[825];

        this.parabolicField = new float[25];
        for (int j = -2; j <= 2; ++j) {
            for (int k = -2; k <= 2; ++k) {
                float f = 10.0F / MathHelper.sqrt_float((j * j + k * k) + 0.2F);
                this.parabolicField[j + 2 + (k + 2) * 5] = f;
            }
        }
    }

    @Override
    public void setup(World world, GenericChunkProvider provider) {
        this.world = world;
        this.provider = provider;

        this.noiseGen1 = new NoiseGeneratorOctaves(provider.rand, 16);
        this.noiseGen2 = new NoiseGeneratorOctaves(provider.rand, 16);
        this.noiseGen3 = new NoiseGeneratorOctaves(provider.rand, 8);
        this.noiseGen4 = new NoiseGeneratorPerlin(provider.rand, 4);
        NoiseGeneratorOctaves noiseGen5 = new NoiseGeneratorOctaves(provider.rand, 10);
        this.noiseGen6 = new NoiseGeneratorOctaves(provider.rand, 16);
        NoiseGeneratorOctaves mobSpawnerNoise = new NoiseGeneratorOctaves(provider.rand, 8);

        NoiseGenerator[] noiseGens = {noiseGen1, noiseGen2, noiseGen3, noiseGen4, noiseGen5, noiseGen6, mobSpawnerNoise};
        noiseGens = TerrainGen.getModdedNoiseGenerators(world, provider.rand, noiseGens);
        this.noiseGen1 = (NoiseGeneratorOctaves) noiseGens[0];
        this.noiseGen2 = (NoiseGeneratorOctaves) noiseGens[1];
        this.noiseGen3 = (NoiseGeneratorOctaves) noiseGens[2];
        this.noiseGen4 = (NoiseGeneratorPerlin) noiseGens[3];
        this.noiseGen6 = (NoiseGeneratorOctaves) noiseGens[5];
    }

    private void func_147423_a(int chunkX4, int chunkY4, int chunkZ4) {
        this.noiseData4 = this.noiseGen6.generateNoiseOctaves(this.noiseData4, chunkX4, chunkZ4, 5, 5, 200.0D, 200.0D, 0.5D);
        this.noiseData1 = this.noiseGen3.generateNoiseOctaves(this.noiseData1, chunkX4, chunkY4, chunkZ4, 5, 33, 5, 8.555150000000001D, 4.277575000000001D, 8.555150000000001D);
        this.noiseData2 = this.noiseGen1.generateNoiseOctaves(this.noiseData2, chunkX4, chunkY4, chunkZ4, 5, 33, 5, 684.412D, 684.412D, 684.412D);
        this.noiseData3 = this.noiseGen2.generateNoiseOctaves(this.noiseData3, chunkX4, chunkY4, chunkZ4, 5, 33, 5, 684.412D, 684.412D, 684.412D);
        int l = 0;
        int i1 = 0;

        boolean domaze = false;
        boolean elevated = false;
        if (provider.dimensionInformation.hasFeatureType(FeatureType.FEATURE_MAZE)) {
            domaze = true;
            long s2 = (((chunkX4 >> 2) + provider.seed + 13) * 314) + (chunkZ4 >> 2) * 17L;
            Random rand = new Random(s2);
            rand.nextFloat();   // Skip one.
            elevated = ((chunkX4 >> 2) & 1) == 0;
            if (rand.nextFloat() < .2f) {
                elevated = !elevated;
            }
        }

        for (int j1 = 0; j1 < 5; ++j1) {
            for (int k1 = 0; k1 < 5; ++k1) {
                float f = 0.0F;
                float f1 = 0.0F;
                float f2 = 0.0F;
                byte b0 = 2;
                BiomeGenBase biomegenbase = provider.biomesForGeneration[j1 + 2 + (k1 + 2) * 10];

                for (int l1 = -b0; l1 <= b0; ++l1) {
                    for (int i2 = -b0; i2 <= b0; ++i2) {
                        BiomeGenBase biomegenbase1 = provider.biomesForGeneration[j1 + l1 + 2 + (k1 + i2 + 2) * 10];
                        float f3 = biomegenbase1.rootHeight;
                        float f4 = biomegenbase1.heightVariation;

                        if (domaze) {
                            if (f3 > 0.0F && elevated) {
                                if (provider.worldType == WorldType.AMPLIFIED) {
                                    f3 = 2.0F + f3 * 1.5f;
                                    f4 = 1.0F + f4 * 3.0f;
                                } else {
                                    f3 = 2.0F + f3;
                                    f4 = 0.5F + f4 * 1.5f;
                                }
                            } else {
                                if (provider.worldType == WorldType.AMPLIFIED && f3 > 0.0f) {
                                    f3 = 0.5F + f3 * 1.5F;
                                    f4 = 0.5F + f4 * 2.0F;
                                } else {
                                    f4 = f4 * 0.5F;
                                }
                            }
                        } else {
                            if (provider.worldType == WorldType.AMPLIFIED && f3 > 0.0F) {
                                f3 = 1.0F + f3 * 2.0F;
                                f4 = 1.0F + f4 * 4.0F;
                            }
                        }

                        float f5 = parabolicField[l1 + 2 + (i2 + 2) * 5] / (f3 + 2.0F);

                        if (biomegenbase1.rootHeight > biomegenbase.rootHeight) {
                            f5 /= 2.0F;
                        }

                        f += f4 * f5;
                        f1 += f3 * f5;
                        f2 += f5;
                    }
                }

                f /= f2;
                f1 /= f2;
                f = f * 0.9F + 0.1F;
                f1 = (f1 * 4.0F - 1.0F) / 8.0F;
                double d12 = this.noiseData4[i1] / 8000.0D;

                if (d12 < 0.0D) {
                    d12 = -d12 * 0.3D;
                }

                d12 = d12 * 3.0D - 2.0D;

                if (d12 < 0.0D) {
                    d12 /= 2.0D;

                    if (d12 < -1.0D) {
                        d12 = -1.0D;
                    }

                    d12 /= 1.4D;
                    d12 /= 2.0D;
                } else {
                    if (d12 > 1.0D) {
                        d12 = 1.0D;
                    }

                    d12 /= 8.0D;
                }

                ++i1;
                double d13 = f1;
                double d14 = f;
                d13 += d12 * 0.2D;
                d13 = d13 * 8.5D / 8.0D;
                double d5 = 8.5D + d13 * 4.0D;

                for (int j2 = 0; j2 < 33; ++j2) {
                    double d6 = (j2 - d5) * 12.0D * 128.0D / 256.0D / d14;

                    if (d6 < 0.0D) {
                        d6 *= 4.0D;
                    }

                    double d7 = this.noiseData2[l] / 512.0D;
                    double d8 = this.noiseData3[l] / 512.0D;
                    double d9 = (this.noiseData1[l] / 10.0D + 1.0D) / 2.0D;
                    double d10 = MathHelper.denormalizeClamp(d7, d8, d9) - d6;

                    if (j2 > 29) {
                        double d11 = ((j2 - 29) / 3.0F);
                        d10 = d10 * (1.0D - d11) + -10.0D * d11;
                    }

                    this.noiseField[l] = d10;
                    ++l;
                }
            }
        }
    }

    @Override
    public void generate(int chunkX, int chunkZ, Block[] aBlock, byte[] meta) {
        Block baseBlock = provider.dimensionInformation.getBaseBlockForTerrain().getBlock();
        byte baseMeta = provider.dimensionInformation.getBaseBlockForTerrain().getMeta();
        Block baseLiquid = provider.dimensionInformation.getFluidForTerrain();

        func_147423_a(chunkX * 4, 0, chunkZ * 4);

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
                    double d1 = noiseField[k1 + height32];
                    double d2 = noiseField[l1 + height32];
                    double d3 = noiseField[i2 + height32];
                    double d4 = noiseField[j2 + height32];
                    double d5 = (noiseField[k1 + height32 + 1] - d1) * d0;
                    double d6 = (noiseField[l1 + height32 + 1] - d2) * d0;
                    double d7 = (noiseField[i2 + height32 + 1] - d3) * d0;
                    double d8 = (noiseField[j2 + height32 + 1] - d4) * d0;

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
                                index += maxheight;
                                if ((d15 += d16) > 0.0D) {
                                    aBlock[index] = baseBlock;
                                    meta[index] = baseMeta;
                                } else if (height < waterLevel) {
                                    aBlock[index] = baseLiquid;
                                } else {
                                    aBlock[index] = null;
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

    @Override
    public void replaceBlocksForBiome(int chunkX, int chunkZ, Block[] aBlock, byte[] abyte, BiomeGenBase[] biomeGenBases) {
        ChunkProviderEvent.ReplaceBiomeBlocks event = new ChunkProviderEvent.ReplaceBiomeBlocks(provider, chunkX, chunkZ, aBlock, abyte, biomeGenBases, world);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Event.Result.DENY) {
            return;
        }

        double d0 = 0.03125D;
        this.stoneNoise = this.noiseGen4.func_151599_a(this.stoneNoise, (chunkX * 16), (chunkZ * 16), 16, 16, d0 * 2.0D, d0 * 2.0D, 1.0D);

        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                BiomeGenBase biomegenbase = biomeGenBases[l + k * 16];
                biomegenbase.genTerrainBlocks(world, provider.rand, aBlock, abyte, chunkX * 16 + k, chunkZ * 16 + l, this.stoneNoise[l + k * 16]);
            }
        }
    }

}
