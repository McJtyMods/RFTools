package com.mcjty.rftools.dimension.world.mapgen;

import com.mcjty.rftools.dimension.world.GenericChunkProvider;
import com.mcjty.varia.BlockMeta;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.Random;

public class MapGenTendrils {
    private final GenericChunkProvider provider;
    private int range = 8;
    private Random rand = new Random();


    public MapGenTendrils(GenericChunkProvider provider) {
        this.provider = provider;
    }

    private void func_151541_a(long seed, int chunkX, int chunkZ, Block[] data, byte[] meta, double p_151541_6_, double p_151541_8_, double p_151541_10_, float p_151541_12_, float p_151541_13_, float p_151541_14_, int p_151541_15_, int p_151541_16_, double p_151541_17_) {
        BlockMeta baseBlock = provider.dimensionInformation.getTendrilBlock();

        double d4 = (chunkX * 16 + 8);
        double d5 = (chunkZ * 16 + 8);
        float f3 = 0.0F;
        float f4 = 0.0F;
        Random random = new Random(seed);

        if (p_151541_16_ <= 0) {
            int j1 = this.range * 16 - 16;
            p_151541_16_ = j1 - random.nextInt(j1 / 4);
        }

        boolean flag2 = false;

        if (p_151541_15_ == -1) {
            p_151541_15_ = p_151541_16_ / 2;
            flag2 = true;
        }

        int k1 = random.nextInt(p_151541_16_ / 2) + p_151541_16_ / 4;

        boolean flag = random.nextInt(6) == 0;
        while (p_151541_15_ < p_151541_16_) {
            double d6 = 1.5D + (MathHelper.sin(p_151541_15_ * (float) Math.PI / p_151541_16_) * p_151541_12_ * 1.0F);
            double d7 = d6 * p_151541_17_;
            float f5 = MathHelper.cos(p_151541_14_);
            float f6 = MathHelper.sin(p_151541_14_);
            p_151541_6_ += (MathHelper.cos(p_151541_13_) * f5);
            p_151541_8_ += f6;
            p_151541_10_ += (MathHelper.sin(p_151541_13_) * f5);

            if (flag) {
                p_151541_14_ *= 0.92F;
            } else {
                p_151541_14_ *= 0.7F;
            }

            p_151541_14_ += f4 * 0.1F;
            p_151541_13_ += f3 * 0.1F;
            f4 *= 0.9F;
            f3 *= 0.75F;
            f4 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            f3 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

            if (!flag2 && p_151541_15_ == k1 && p_151541_12_ > 1.0F && p_151541_16_ > 0) {
                this.func_151541_a(random.nextLong(), chunkX, chunkZ, data, meta, p_151541_6_, p_151541_8_, p_151541_10_, random.nextFloat() * 0.5F + 0.5F, p_151541_13_ - ((float)Math.PI / 2F), p_151541_14_ / 3.0F, p_151541_15_, p_151541_16_, 1.0D);
                this.func_151541_a(random.nextLong(), chunkX, chunkZ, data, meta, p_151541_6_, p_151541_8_, p_151541_10_, random.nextFloat() * 0.5F + 0.5F, p_151541_13_ + ((float)Math.PI / 2F), p_151541_14_ / 3.0F, p_151541_15_, p_151541_16_, 1.0D);
                return;
            }

            if (flag2 || random.nextInt(4) != 0) {
                double d8 = p_151541_6_ - d4;
                double d9 = p_151541_10_ - d5;
                double d10 = (p_151541_16_ - p_151541_15_);
                double d11 = (p_151541_12_ + 2.0F + 16.0F);

                if (d8 * d8 + d9 * d9 - d10 * d10 > d11 * d11) {
                    return;
                }

                if (p_151541_6_ >= d4 - 16.0D - d6 * 2.0D && p_151541_10_ >= d5 - 16.0D - d6 * 2.0D && p_151541_6_ <= d4 + 16.0D + d6 * 2.0D && p_151541_10_ <= d5 + 16.0D + d6 * 2.0D) {
                    int i4 = MathHelper.floor_double(p_151541_6_ - d6) - chunkX * 16 - 1;
                    int l1 = MathHelper.floor_double(p_151541_6_ + d6) - chunkX * 16 + 1;
                    int j4 = MathHelper.floor_double(p_151541_8_ - d7) - 1;
                    int i2 = MathHelper.floor_double(p_151541_8_ + d7) + 1;
                    int k4 = MathHelper.floor_double(p_151541_10_ - d6) - chunkZ * 16 - 1;
                    int j2 = MathHelper.floor_double(p_151541_10_ + d6) - chunkZ * 16 + 1;

                    if (i4 < 0) {
                        i4 = 0;
                    }

                    if (l1 > 16) {
                        l1 = 16;
                    }

                    if (j4 < 1) {
                        j4 = 1;
                    }

                    if (i2 > 248) {
                        i2 = 248;
                    }

                    if (k4 < 0) {
                        k4 = 0;
                    }

                    if (j2 > 16) {
                        j2 = 16;
                    }

                    int k2;
                    int j3;

                    for (k2 = i4; k2 < l1; ++k2) {
                        double d13 = ((k2 + chunkX * 16) + 0.5D - p_151541_6_) / d6;

                        for (j3 = k4; j3 < j2; ++j3) {
                            double d14 = ((j3 + chunkZ * 16) + 0.5D - p_151541_10_) / d6;
                            int k3 = (k2 * 16 + j3) * 256 + i2;

                            if (d13 * d13 + d14 * d14 < 1.0D) {
                                for (int l3 = i2 - 1; l3 >= j4; --l3) {
                                    double d12 = (l3 + 0.5D - p_151541_8_) / d7;

                                    if (d12 > -0.7D && d13 * d13 + d12 * d12 + d14 * d14 < 1.0D) {
                                        Block block  = data[k3];

                                        if (block == Blocks.air || block == null) {
                                            data[k3] = baseBlock.getBlock();
                                            meta[k3] = baseBlock.getMeta();
                                        }
                                    }

                                    --k3;
                                }
                            }
                        }

                        if (flag2) {
                            break;
                        }
                    }
                }
            }
            ++p_151541_15_;
        }
    }

    public void generate(World world, int chunkX, int chunkZ, Block[] ablock, byte[] ameta) {
        int k = this.range;
        this.rand.setSeed(world.getSeed());
        long l = this.rand.nextLong();
        long i1 = this.rand.nextLong();

        for (int cx = chunkX - k; cx <= chunkX + k; ++cx) {
            for (int cz = chunkZ - k; cz <= chunkZ + k; ++cz) {
                long l1 = cx * l;
                long i2 = cz * i1;
                this.rand.setSeed(l1 ^ i2 ^ world.getSeed());
                this.fillChunk(cx, cz, chunkX, chunkZ, ablock, ameta);
            }
        }
    }

    private void fillChunk(int cx, int cz, int chunkX, int chunkZ, Block[] data, byte[] meta) {
        int i1 = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(15) + 1) + 1);

        if (this.rand.nextInt(7) != 0) {
            i1 = 0;
        }

        for (int j1 = 0; j1 < i1; ++j1) {
            double d0 = (cx * 16 + this.rand.nextInt(16));
            double d1 = this.rand.nextInt(this.rand.nextInt(120) + 8);
            double d2 = (cz * 16 + this.rand.nextInt(16));
            int k1 = 1;

            if (this.rand.nextInt(4) == 0) {
                this.func_151541_a(this.rand.nextLong(), chunkX, chunkZ, data, meta, d0, d1, d2, 1.0F + this.rand.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5D);
                k1 += this.rand.nextInt(4);
            }

            for (int l1 = 0; l1 < k1; ++l1) {
                float f = this.rand.nextFloat() * (float)Math.PI * 2.0F;
                float f1 = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float f2 = this.rand.nextFloat() * 2.0F + this.rand.nextFloat();

                if (this.rand.nextInt(10) == 0) {
                    f2 *= this.rand.nextFloat() * this.rand.nextFloat() * 3.0F + 1.0F;
                }

                this.func_151541_a(this.rand.nextLong(), chunkX, chunkZ, data, meta, d0, d1, d2, f2, f, f1, 0, 0, 1.0D);
            }
        }
    }

}
