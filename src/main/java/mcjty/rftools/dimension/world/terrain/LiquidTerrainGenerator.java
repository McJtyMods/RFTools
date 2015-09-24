package mcjty.rftools.dimension.world.terrain;

import mcjty.rftools.dimension.world.GenericChunkProvider;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class LiquidTerrainGenerator implements BaseTerrainGenerator {
    protected GenericChunkProvider provider;

    @Override
    public void setup(World world, GenericChunkProvider provider) {
        this.provider = provider;
    }

    @Override
    public void generate(int chunkX, int chunkZ, Block[] aBlock, byte[] abyte) {
        Block baseLiquid = provider.dimensionInformation.getFluidForTerrain();

        byte waterLevel = 127;

        int index = 0;
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int height = 0;
                while (height < 1) {
                    aBlock[index] = Blocks.bedrock;
                    abyte[index++] = 0;
                    height++;
                }
                while (height < waterLevel) {
                    aBlock[index] = baseLiquid;
                    abyte[index++] = 0;
                    height++;
                }
                while (height < 256) {
                    aBlock[index] = null;
                    abyte[index++] = 0;
                    height++;
                }
            }
        }

    }

    @Override
    public void replaceBlocksForBiome(int chunkX, int chunkZ, Block[] aBlock, byte[] abyte, BiomeGenBase[] biomeGenBases) {
    }

}
