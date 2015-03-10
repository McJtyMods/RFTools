package com.mcjty.rftools.dimension.world;

import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SingleBiomeWorldChunkManager extends WorldChunkManager {
    private BiomeGenBase biomeGenBase;

    public SingleBiomeWorldChunkManager(World world, long seed, WorldType worldType) {
        super(seed, worldType);
        DimensionInformation dimensionInformation = RfToolsDimensionManager.getDimensionManager(world).getDimensionInformation(world.provider.dimensionId);
        biomeGenBase = dimensionInformation.getBiomes().get(0);
    }

    /**
     * Returns the BiomeGenBase related to the x, z position on the world.
     */
    @Override
    public BiomeGenBase getBiomeGenAt(int x, int z) {
        return this.biomeGenBase;
    }

    /**
     * Returns an array of biomes for the location input.
     */
    @Override
    public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] biomeArray, int p_76937_2_, int p_76937_3_, int sizex, int sizez) {
        if (biomeArray == null || biomeArray.length < sizex * sizez) {
            biomeArray = new BiomeGenBase[sizex * sizez];
        }

        Arrays.fill(biomeArray, 0, sizex * sizez, this.biomeGenBase);
        return biomeArray;
    }

    /**
     * Returns a list of rainfall values for the specified blocks. Args: listToReuse, x, z, width, length.
     */
    @Override
    public float[] getRainfall(float[] p_76936_1_, int p_76936_2_, int p_76936_3_, int p_76936_4_, int p_76936_5_) {
        if (p_76936_1_ == null || p_76936_1_.length < p_76936_4_ * p_76936_5_) {
            p_76936_1_ = new float[p_76936_4_ * p_76936_5_];
        }

        Arrays.fill(p_76936_1_, 0, p_76936_4_ * p_76936_5_, 0.0f);
        return p_76936_1_;
    }

    /**
     * Returns biomes to use for the blocks and loads the other data like temperature and humidity onto the
     * WorldChunkManager Args: oldBiomeList, x, z, width, depth
     */
    @Override
    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] p_76933_1_, int p_76933_2_, int p_76933_3_, int p_76933_4_, int p_76933_5_) {
        if (p_76933_1_ == null || p_76933_1_.length < p_76933_4_ * p_76933_5_) {
            p_76933_1_ = new BiomeGenBase[p_76933_4_ * p_76933_5_];
        }

        Arrays.fill(p_76933_1_, 0, p_76933_4_ * p_76933_5_, this.biomeGenBase);
        return p_76933_1_;
    }

    /**
     * Return a list of biomes for the specified blocks. Args: listToReuse, x, y, width, length, cacheFlag (if false,
     * don't check biomeCache to avoid infinite loop in BiomeCacheBlock)
     */
    @Override
    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] p_76931_1_, int p_76931_2_, int p_76931_3_, int p_76931_4_, int p_76931_5_, boolean p_76931_6_) {
        return this.loadBlockGeneratorData(p_76931_1_, p_76931_2_, p_76931_3_, p_76931_4_, p_76931_5_);
    }

    @Override
    public ChunkPosition findBiomePosition(int p_150795_1_, int p_150795_2_, int p_150795_3_, List p_150795_4_, Random p_150795_5_) {
        return p_150795_4_.contains(this.biomeGenBase) ? new ChunkPosition(p_150795_1_ - p_150795_3_ + p_150795_5_.nextInt(p_150795_3_ * 2 + 1), 0, p_150795_2_ - p_150795_3_ + p_150795_5_.nextInt(p_150795_3_ * 2 + 1)) : null;
    }

    /**
     * checks given Chunk's Biomes against List of allowed ones
     */
    @Override
    public boolean areBiomesViable(int p_76940_1_, int p_76940_2_, int p_76940_3_, List p_76940_4_) {
        return p_76940_4_.contains(this.biomeGenBase);
    }

}
