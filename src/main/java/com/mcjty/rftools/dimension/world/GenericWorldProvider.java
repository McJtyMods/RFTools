package com.mcjty.rftools.dimension.world;

import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.rftools.dimension.SkyDescriptor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.DimensionManager;

public class GenericWorldProvider extends WorldProvider {
    private DimensionInformation dimensionInformation;

    private long calculateSeed(long seed, int dim) {
        return dim * 13 + seed;
    }

    @Override
    public void registerWorldChunkManager() {
        int dim = worldObj.provider.dimensionId;
        long seed = calculateSeed(worldObj.getSeed(), dim);

        dimensionInformation = RfToolsDimensionManager.getDimensionManager(worldObj).getDimensionInformation(dim);

        if (dimensionInformation != null && !dimensionInformation.getBiomes().isEmpty()) {
            worldChunkMgr = new SingleBiomeWorldChunkManager(worldObj, seed, terrainType);
        } else {
            worldChunkMgr = new WorldChunkManager(seed, worldObj.getWorldInfo().getTerrainType());
        }
        hasNoSky = false;
    }

    public static WorldProvider getProviderForDimension(int id) {
        return DimensionManager.createProviderFor(id);
    }

    @Override
    public String getDimensionName() {
        return "rftools dimension";
    }

    @Override
    public String getWelcomeMessage() {
        return "Entering the rftools dimension!";
    }

    @Override
    public boolean canRespawnHere() {
        return false;
    }

    @Override
    public IChunkProvider createChunkGenerator() {
        int dim = worldObj.provider.dimensionId;
        long seed = calculateSeed(worldObj.getSeed(), dim);
        return new GenericChunkProvider(worldObj, seed);
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
        return super.getBiomeGenForCoords(x, z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getSunBrightness(float par1) {
        return super.getSunBrightness(par1) * dimensionInformation.getSkyDescriptor().getSunBrightnessFactor();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getStarBrightness(float par1) {
        return super.getStarBrightness(par1) * dimensionInformation.getSkyDescriptor().getStarBrightnessFactor();
    }
}
