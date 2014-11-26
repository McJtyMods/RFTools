package com.mcjty.rftools.dimension.world;

import com.mcjty.rftools.dimension.world.chunk.GenericChunkProvider;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.DimensionManager;

public class GenericWorldProvider extends WorldProvider {

    @Override
    public void registerWorldChunkManager() {
        worldChunkMgr = new GenericWorldChunkManager(worldObj.getSeed(), terrainType);
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
