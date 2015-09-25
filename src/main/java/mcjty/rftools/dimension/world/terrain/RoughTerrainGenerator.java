package mcjty.rftools.dimension.world.terrain;

import mcjty.rftools.blocks.dimlets.DimletConfiguration;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import java.util.Random;

public class RoughTerrainGenerator extends NormalTerrainGenerator {
    private final boolean filled;

    public RoughTerrainGenerator(boolean filled) {
        super();
        this.filled = filled;
    }

    @Override
    public void generate(int chunkX, int chunkZ, Block[] aBlock, byte[] abyte) {
        Block baseBlock = provider.dimensionInformation.getBaseBlockForTerrain().getBlock();
        byte baseMeta = provider.dimensionInformation.getBaseBlockForTerrain().getMeta();
        Block baseFluid = provider.dimensionInformation.getFluidForTerrain();

        Random random = new Random(chunkX * 13L + chunkZ * 577L);

        int index = 0;
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                byte waterLevel = (byte) (63 + random.nextFloat() * 32 - 16);
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
                if (filled) {
                    while (height < 63) {
                        aBlock[index] = baseFluid;
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
