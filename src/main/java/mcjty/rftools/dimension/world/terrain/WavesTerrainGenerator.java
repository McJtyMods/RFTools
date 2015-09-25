package mcjty.rftools.dimension.world.terrain;

import mcjty.rftools.blocks.dimlets.DimletConfiguration;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class WavesTerrainGenerator extends NormalTerrainGenerator {
    @Override
    public void generate(int chunkX, int chunkZ, Block[] aBlock, byte[] abyte) {
        Block baseBlock = provider.dimensionInformation.getBaseBlockForTerrain().getBlock();
        byte baseMeta = provider.dimensionInformation.getBaseBlockForTerrain().getMeta();

        byte waterLevel = 63;

        int index = 0;
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                double sin = Math.sin((chunkX * 16 + x) / 16.0f);
                double cos = Math.cos((chunkZ * 16 + z) / 16.0f);
                waterLevel = (byte) (63 + sin * cos * 16);
                int height = 0;
                while (height < DimletConfiguration.bedrockLayer) {
                    aBlock[index] = Blocks.bedrock;
                    abyte[index++] = 0;
                    height++;
                }
                if (baseMeta == 127) {
                    while (height < waterLevel) {
                        aBlock[index] = baseBlock;
                        abyte[index++] = (byte) ((height/2 + x/2 + z/2) & 0xf);
                        height++;
                    }
                } else {
                    while (height < waterLevel) {
                        aBlock[index] = baseBlock;
                        abyte[index++] = baseMeta;
                        height++;
                    }
                }
                while (height < 256) {
                    aBlock[index++] = null;
                    height++;
                }
            }
        }

    }
}
