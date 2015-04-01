package mcjty.rftools.dimension.world.terrain;

import mcjty.rftools.dimension.world.GenericChunkProvider;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class VoidTerrainGenerator implements BaseTerrainGenerator {

    @Override
    public void setup(World world, GenericChunkProvider provider) {

    }

    @Override
    public void generate(int chunkX, int chunkZ, Block[] aBlock, byte[] meta) {
        for (int i = 0 ; i < 65536 ; i++) {
            aBlock[i] = null;
        }
    }

    @Override
    public void replaceBlocksForBiome(int chunkX, int chunkZ, Block[] aBlock, byte[] abyte, BiomeGenBase[] biomeGenBases) {
        for (int i = 0 ; i < 65536 ; i++) {
            abyte[i] = 0;
        }
    }

}
