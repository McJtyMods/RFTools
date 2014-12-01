package com.mcjty.rftools.dimension.world.terrain;

import com.mcjty.rftools.dimension.world.GenericChunkProvider;
import net.minecraft.block.Block;

/**
 * The base terrain generator.
 */
public interface BaseTerrainGenerator {
    void generate(GenericChunkProvider provider, int chunkX, int chunkZ, Block[] aBlock);
}
