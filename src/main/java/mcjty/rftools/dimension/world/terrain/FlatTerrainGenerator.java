package mcjty.rftools.dimension.world.terrain;

import mcjty.rftools.blocks.dimlets.DimletConfiguration;
import mcjty.rftools.dimension.world.types.FeatureType;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import java.util.Random;

public class FlatTerrainGenerator extends NormalTerrainGenerator {

    @Override
    public void generate(int chunkX, int chunkZ, Block[] aBlock, byte[] abyte) {
        Block baseBlock = provider.dimensionInformation.getBaseBlockForTerrain().getBlock();
        byte baseMeta = provider.dimensionInformation.getBaseBlockForTerrain().getMeta();

        byte waterLevel = 63;

        boolean elevated = false;
        if (provider.dimensionInformation.hasFeatureType(FeatureType.FEATURE_MAZE)) {
            long s2 = ((chunkX + provider.seed + 13) * 314) + chunkZ * 17L;
            Random rand = new Random(s2);
            rand.nextFloat();   // Skip one.
            elevated = (chunkX & 1) == 0;
            if (rand.nextFloat() < .2f) {
                elevated = !elevated;
            }
            if (elevated) {
                waterLevel = 120;
            } else {
                waterLevel = 40;
            }
        }

        int index = 0;
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int height = 0;
                while (height < DimletConfiguration.bedrockLayer) {
                    aBlock[index] = Blocks.bedrock;
                    abyte[index++] = 0;
                    height++;
                }
                while (height < waterLevel) {
                    aBlock[index] = baseBlock;
                    abyte[index++] = baseMeta;
                    height++;
                }
                while (height < 256) {
                    aBlock[index++] = null;
                    height++;
                }
            }
        }

    }
}
