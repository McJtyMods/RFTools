package com.mcjty.rftools.dimension.world;

import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.DimensionManager;

public class GenericWorldProvider extends WorldProvider {

    @Override
    public void registerWorldChunkManager() {
        System.out.println("worldObj.isRemote = " + worldObj.isRemote);
        System.out.println("worldObj = " + worldObj); System.out.flush();
        System.out.println("worldObj.provider = " + worldObj.provider); System.out.flush();
        DimensionInformation dimensionInformation = RfToolsDimensionManager.getDimensionManager(worldObj).getDimensionInformation(worldObj.provider.dimensionId);
        if (dimensionInformation != null && !dimensionInformation.getBiomes().isEmpty()) {
            worldChunkMgr = new SingleBiomeWorldChunkManager(worldObj, terrainType);
        } else {
//        worldChunkMgr = new GenericWorldChunkManager(worldObj.getSeed(), terrainType, worldObj);
            worldChunkMgr = new WorldChunkManager(worldObj);
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
        return new GenericChunkProvider(worldObj, worldObj.getSeed());
    }
}
