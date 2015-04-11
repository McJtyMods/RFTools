package mcjty.rftools.dimension.world.mapgen;

import mcjty.rftools.dimension.world.GenericChunkProvider;
import mcjty.varia.BlockMeta;
import net.minecraft.block.Block;
import net.minecraft.world.World;

import java.util.Random;

public class MapGenOrbs {
    private final GenericChunkProvider provider;
    private final boolean large;
    private final int r;

    public MapGenOrbs(GenericChunkProvider provider, boolean large) {
        this.provider = provider;
        this.large = large;
        r = large ? 2 : 1;
    }

    public void generate(World world, int chunkX, int chunkZ, Block[] ablock, byte[] ameta) {
        BlockMeta[] blocks = large ? provider.dimensionInformation.getHugeSphereBlocks() : provider.dimensionInformation.getSphereBlocks();

        for (int cx = -r ; cx <= r ; cx++) {
            for (int cz = -r ; cz <= r ; cz++) {

                Random random = new Random((world.getSeed() + (chunkX+cx)) * 113 + (chunkZ+cz) * 31 + 77);
                random.nextFloat();

                if (random.nextFloat() < .05f) {
                    int x = cx * 16 + random.nextInt(16);
                    int y = 40 + random.nextInt(40);
                    int z = cz * 16 + random.nextInt(16);
                    int radius = random.nextInt(large ? 20 : 6) + (large ? 10 : 4);
                    int index = 0;
                    if (blocks.length > 1) {
                        index = random.nextInt(blocks.length);
                    }

                    fillSphere(ablock, ameta, x, y, z, radius, blocks[index]);
                }
            }
        }
    }

    private void fillSphere(Block[] ablock, byte[] ameta, int centerx, int centery, int centerz, int radius, BlockMeta blockMeta) {
        Block block = blockMeta.getBlock();
        byte meta = blockMeta.getMeta();

        double sqradius = radius * radius;

        for (int x = 0 ; x < 16 ; x++) {
            double dxdx = (x-centerx) * (x-centerx);
            for (int z = 0 ; z < 16 ; z++) {
                double dzdz = (z-centerz) * (z-centerz);
                int index = (x * 16 + z) * 256;
                for (int y = centery-radius ; y <= centery+radius ; y++) {
                    double dydy = (y-centery) * (y-centery);
                    double sqdist = dxdx + dydy + dzdz;
                    if (sqdist <= sqradius) {
                        ablock[index + y] = block;
                        ameta[index + y] = meta;
                    }
                }
            }
        }
    }

}
