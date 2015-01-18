package com.mcjty.rftools.dimension.world;

import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraftforge.common.BiomeManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GenericWorldChunkManager extends WorldChunkManager {
    private DimensionInformation dimensionInformation = null;
    private final World world;

    private DimensionInformation getDimensionInformation() {
        if (dimensionInformation == null) {
            dimensionInformation = RfToolsDimensionManager.getDimensionManager(world).getDimensionInformation(world.provider.dimensionId);
        }
        return dimensionInformation;
    }

    public GenericWorldChunkManager(long seed, WorldType worldType, World world) {
        super(seed, worldType);
        this.world = world;
    }

    @Override
    public List getBiomesToSpawnIn() {
        return super.getBiomesToSpawnIn();
    }

    /**
     * Returns the BiomeGenBase related to the x, z position on the world.
     */
    @Override
    public BiomeGenBase getBiomeGenAt(int x, int z) {
        return super.getBiomeGenAt(x, z);
    }

    @Override
    public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] p_76937_1_, int p_76937_2_, int p_76937_3_, int p_76937_4_, int p_76937_5_) {
        return super.getBiomesForGeneration(p_76937_1_, p_76937_2_, p_76937_3_, p_76937_4_, p_76937_5_);
    }

    /**
     * Returns a list of rainfall values for the specified blocks. Args: listToReuse, x, z, width, length.
     */
    @Override
    public float[] getRainfall(float[] p_76936_1_, int p_76936_2_, int p_76936_3_, int p_76936_4_, int p_76936_5_) {
        return super.getRainfall(p_76936_1_, p_76936_2_, p_76936_3_, p_76936_4_, p_76936_5_);
    }

    @Override
    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] p_76933_1_, int p_76933_2_, int p_76933_3_, int p_76933_4_, int p_76933_5_) {
        return super.loadBlockGeneratorData(p_76933_1_, p_76933_2_, p_76933_3_, p_76933_4_, p_76933_5_);
    }

    @Override
    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] p_76931_1_, int p_76931_2_, int p_76931_3_, int p_76931_4_, int p_76931_5_, boolean p_76931_6_) {
        return super.getBiomeGenAt(p_76931_1_, p_76931_2_, p_76931_3_, p_76931_4_, p_76931_5_, p_76931_6_);
    }

    @Override
    public boolean areBiomesViable(int p_76940_1_, int p_76940_2_, int p_76940_3_, List p_76940_4_) {
        return super.areBiomesViable(p_76940_1_, p_76940_2_, p_76940_3_, p_76940_4_);
    }

    @Override
    public ChunkPosition findBiomePosition(int p_150795_1_, int p_150795_2_, int p_150795_3_, List p_150795_4_, Random p_150795_5_) {
        return super.findBiomePosition(p_150795_1_, p_150795_2_, p_150795_3_, p_150795_4_, p_150795_5_);
    }

    @Override
    public GenLayer[] getModdedBiomeGenerators(WorldType worldType, long seed, GenLayer[] original) {
        GenLayer[] layer = super.getModdedBiomeGenerators(worldType, seed, original);
        RFToolsGenLayer rfToolsGenLayer = new RFToolsGenLayer(seed, layer[0]);
        GenLayerVoronoiZoom zoomLayer = new GenLayerVoronoiZoom(10L, rfToolsGenLayer);
        zoomLayer.initWorldGenSeed(seed);
        return new GenLayer[] {rfToolsGenLayer, zoomLayer, rfToolsGenLayer};
    }
}
