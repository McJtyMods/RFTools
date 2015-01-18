package com.mcjty.rftools.dimension.world.terrain;

import com.mcjty.rftools.dimension.world.GenericChunkProvider;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

/**
 * The base terrain generator.
 */
public interface BaseTerrainGenerator {
    void setup(World world, GenericChunkProvider provider);

    void generate(int chunkX, int chunkZ, Block[] aBlock, byte[] abyte);

    void replaceBlocksForBiome(int chunkX, int chunkZ, Block[] aBlock, byte[] abyte, BiomeGenBase[] biomeGenBases);
}
