package com.mcjty.rftools.dimension.world.terrain;

import com.mcjty.rftools.dimension.world.GenericChunkProvider;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.NoiseGenerator;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.ChunkProviderEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

public class IslandTerrainGenerator implements BaseTerrainGenerator {
    private World world;
    GenericChunkProvider provider;

    private double[] densities;

    private NoiseGeneratorOctaves noiseGen1;
    private NoiseGeneratorOctaves noiseGen2;
    private NoiseGeneratorOctaves noiseGen3;
    public NoiseGeneratorOctaves noiseGen4;
    public NoiseGeneratorOctaves noiseGen5;

    private double[] stoneNoise = new double[256];
    double[] noiseData1;
    double[] noiseData2;
    double[] noiseData3;
    double[] noiseData4;
    double[] noiseData5;

    @Override
    public void setup(World world, GenericChunkProvider provider) {
        this.world = world;
        this.provider = provider;

        this.noiseGen1 = new NoiseGeneratorOctaves(provider.rand, 16);
        this.noiseGen2 = new NoiseGeneratorOctaves(provider.rand, 16);
        this.noiseGen3 = new NoiseGeneratorOctaves(provider.rand, 8);
        this.noiseGen4 = new NoiseGeneratorOctaves(provider.rand, 10);
        this.noiseGen5 = new NoiseGeneratorOctaves(provider.rand, 16);

        NoiseGenerator[] noiseGens = {noiseGen1, noiseGen2, noiseGen3, noiseGen4, noiseGen5};
        noiseGens = TerrainGen.getModdedNoiseGenerators(world, provider.rand, noiseGens);
        this.noiseGen1 = (NoiseGeneratorOctaves)noiseGens[0];
        this.noiseGen2 = (NoiseGeneratorOctaves)noiseGens[1];
        this.noiseGen3 = (NoiseGeneratorOctaves)noiseGens[2];
        this.noiseGen4 = (NoiseGeneratorOctaves)noiseGens[3];
        this.noiseGen5 = (NoiseGeneratorOctaves)noiseGens[4];
    }

    /**
     * generates a subset of the level's terrain data. Takes 7 arguments: the [empty] noise array, the position, and the
     * size.
     */
    private double[] initializeNoiseField(double[] densities, int chunkX2, int chunkY2, int chunkZ2, int sizeX, int sizeY, int sizeZ) {
        ChunkProviderEvent.InitNoiseField event = new ChunkProviderEvent.InitNoiseField(provider, densities, chunkX2, chunkY2, chunkZ2, sizeX, sizeY, sizeZ);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Event.Result.DENY) return event.noisefield;

        if (densities == null) {
            densities = new double[sizeX * sizeY * sizeZ];
        }

        double d0 = 684.412D;
        double d1 = 684.412D;
        this.noiseData4 = this.noiseGen4.generateNoiseOctaves(this.noiseData4, chunkX2, chunkZ2, sizeX, sizeZ, 1.121D, 1.121D, 0.5D);
        this.noiseData5 = this.noiseGen5.generateNoiseOctaves(this.noiseData5, chunkX2, chunkZ2, sizeX, sizeZ, 200.0D, 200.0D, 0.5D);
        d0 *= 2.0D;
        this.noiseData1 = this.noiseGen3.generateNoiseOctaves(this.noiseData1, chunkX2, chunkY2, chunkZ2, sizeX, sizeY, sizeZ, d0 / 80.0D, d1 / 160.0D, d0 / 80.0D);
        this.noiseData2 = this.noiseGen1.generateNoiseOctaves(this.noiseData2, chunkX2, chunkY2, chunkZ2, sizeX, sizeY, sizeZ, d0, d1, d0);
        this.noiseData3 = this.noiseGen2.generateNoiseOctaves(this.noiseData3, chunkX2, chunkY2, chunkZ2, sizeX, sizeY, sizeZ, d0, d1, d0);
        int k1 = 0;

        for (int i2 = 0; i2 < sizeX; ++i2) {
            for (int j2 = 0; j2 < sizeZ; ++j2) {
                float f = (i2 + chunkX2) / 1.0F;
                float f1 = (j2 + chunkZ2) / 1.0F;
                float f2 = 100.0F - MathHelper.sqrt_float(f * f + f1 * f1) * 8.0F;

                if (f2 > 80.0F) {
                    f2 = 80.0F;
                }

                if (f2 < -100.0F) {
                    f2 = -100.0F;
                }

                for (int k2 = 0; k2 < sizeY; ++k2) {
                    double d5 = 0.0D;

                    double d7 = this.noiseData2[k1] / 512.0D;
                    double d8 = this.noiseData3[k1] / 512.0D;
                    double d9 = (this.noiseData1[k1] / 10.0D + 1.0D) / 2.0D;

                    if (d9 < 0.0D) {
                        d5 = d7;
                    }
                    else if (d9 > 1.0D) {
                        d5 = d8;
                    } else {
                        d5 = d7 + (d8 - d7) * d9;
                    }

                    d5 -= 8.0D;
                    d5 += f2;
                    byte b0 = 2;
                    double d10;

                    if (k2 > sizeY / 2 - b0) {
                        d10 = ((k2 - (sizeY / 2 - b0)) / 64.0F);

                        if (d10 < 0.0D) {
                            d10 = 0.0D;
                        }

                        if (d10 > 1.0D) {
                            d10 = 1.0D;
                        }

                        d5 = d5 * (1.0D - d10) + -3000.0D * d10;
                    }

                    b0 = 8;

                    if (k2 < b0) {
                        d10 = ((b0 - k2) / (b0 - 1.0F));
                        d5 = d5 * (1.0D - d10) + -30.0D * d10;
                    }

                    densities[k1] = d5;
                    ++k1;
                }
            }
        }

        return densities;
    }

    @Override
    public void generate(int chunkX, int chunkZ, Block[] aBlock) {
        Block baseBlock = provider.dimensionInformation.getBaseBlockForTerrain();

        byte b0 = 2;
        int k = b0 + 1;
        byte b1 = 33;
        int l = b0 + 1;
        this.densities = this.initializeNoiseField(this.densities, chunkX * b0, 0, chunkZ * b0, k, b1, l);

        for (int x2 = 0; x2 < b0; ++x2) {
            for (int z2 = 0; z2 < b0; ++z2) {
                for (int height32 = 0; height32 < 32; ++height32) {
                    double d0 = 0.25D;
                    double d1 = this.densities[((x2 + 0) * l + z2 + 0) * b1 + height32 + 0];
                    double d2 = this.densities[((x2 + 0) * l + z2 + 1) * b1 + height32 + 0];
                    double d3 = this.densities[((x2 + 1) * l + z2 + 0) * b1 + height32 + 0];
                    double d4 = this.densities[((x2 + 1) * l + z2 + 1) * b1 + height32 + 0];
                    double d5 = (this.densities[((x2 + 0) * l + z2 + 0) * b1 + height32 + 1] - d1) * d0;
                    double d6 = (this.densities[((x2 + 0) * l + z2 + 1) * b1 + height32 + 1] - d2) * d0;
                    double d7 = (this.densities[((x2 + 1) * l + z2 + 0) * b1 + height32 + 1] - d3) * d0;
                    double d8 = (this.densities[((x2 + 1) * l + z2 + 1) * b1 + height32 + 1] - d4) * d0;

                    for (int h = 0; h < 8; ++h) {
                        double d9 = 0.125D;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * d9;
                        double d13 = (d4 - d2) * d9;

                        for (int i2 = 0; i2 < 8; ++i2) {
                            int height = (height32 * 4) + h;
                            int index = ((i2 + (x2 * 8)) << 12) | ((0 + (z2 * 8)) << 8) | height;
                            short maxheight = 256;
                            double d14 = 0.125D;
                            double d15 = d10;
                            double d16 = (d11 - d10) * d14;

                            for (int k2 = 0; k2 < 8; ++k2) {
                                Block block = null;

                                if (d15 > 0.0D) {
                                    block = baseBlock;
                                }

                                aBlock[index] = block;
                                index += maxheight;
                                d15 += d16;
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
        this.stoneNoise = this.noiseGen4.generateNoiseOctaves(this.stoneNoise, (chunkX * 16), (chunkZ * 16), 16, 16, d0 * 2.0D, d0 * 2.0D, 1.0D);

        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                BiomeGenBase biomegenbase = biomeGenBases[l + k * 16];
                genBiomeTerrain(biomegenbase, aBlock, abyte, chunkX * 16 + k, chunkZ * 16 + l, this.stoneNoise[l + k * 16]);
            }
        }
    }

    public final void genBiomeTerrain(BiomeGenBase biomegenbase, Block[] blocks, byte[] abyte, int x, int z, double noise) {
        Block baseLiquid = provider.dimensionInformation.getFluidForTerrain();

        boolean flag = true;
        Block block = biomegenbase.topBlock;
        byte b0 = (byte)(biomegenbase.field_150604_aj & 255);
        Block block1 = biomegenbase.fillerBlock;
        int k = -1;
        int l = (int)(noise / 3.0D + 3.0D + provider.rand.nextDouble() * 0.25D);
        int i1 = x & 15;
        int j1 = z & 15;
        int k1 = blocks.length / 256;

        for (int l1 = 255; l1 >= 0; --l1) {
            int i2 = (j1 * 16 + i1) * k1 + l1;

            if (l1 <= 2) {
                blocks[i2] = Blocks.air;
            } else {
                Block block2 = blocks[i2];

                if (block2 != null && block2.getMaterial() != Material.air) {
                    if (block2 == Blocks.stone) {
                        if (k == -1) {
                            if (l <= 0) {
                                block = null;
                                b0 = 0;
                                block1 = Blocks.stone;
                            } else if (l1 >= 59 && l1 <= 64) {
                                block = biomegenbase.topBlock;
                                b0 = (byte)(biomegenbase.field_150604_aj & 255);
                                block1 = biomegenbase.fillerBlock;
                            }

                            if (l1 < 63 && (block == null || block.getMaterial() == Material.air)) {
                                if (biomegenbase.getFloatTemperature(x, l1, z) < 0.15F) {
                                    block = Blocks.ice;
                                    b0 = 0;
                                } else {
                                    block = baseLiquid;
                                    b0 = 0;
                                }
                            }

                            k = l;

                            if (l1 >= 62) {
                                blocks[i2] = block;
                                abyte[i2] = b0;
                            } else if (l1 < 56 - l) {
                                block = null;
                                block1 = Blocks.stone;
                                blocks[i2] = Blocks.gravel;
                            } else {
                                blocks[i2] = block1;
                            }
                        } else if (k > 0) {
                            --k;
                            blocks[i2] = block1;

                            if (k == 0 && block1 == Blocks.sand) {
                                k = provider.rand.nextInt(4) + Math.max(0, l1 - 63);
                                block1 = Blocks.sandstone;
                            }
                        }
                    }
                } else {
                    k = -1;
                }
            }
        }
    }


}