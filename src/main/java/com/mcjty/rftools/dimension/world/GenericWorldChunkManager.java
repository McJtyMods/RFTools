package com.mcjty.rftools.dimension.world;

import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;

public class GenericWorldChunkManager extends WorldChunkManager {

    public GenericWorldChunkManager(long seed, WorldType worldType) {
        super(seed, worldType);
    }
}
