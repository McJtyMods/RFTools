package mcjty.rftools.dimension.world.mapgen;

import mcjty.rftools.dimension.world.GenericChunkProvider;
import mcjty.varia.BlockMeta;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.Random;

public class MapGenLiquidOrbs {
    private final GenericChunkProvider provider;
    private final boolean large;
    private final int r;

    public MapGenLiquidOrbs(GenericChunkProvider provider, boolean large) {
        this.provider = provider;
        this.large = large;
        r = large ? 2 : 1;
    }

    public void generate(World world, int chunkX, int chunkZ, Block[] ablock, byte[] ameta) {
        BlockMeta[] blocks = large ? provider.dimensionInformation.getHugeLiquidSphereBlocks() : provider.dimensionInformation.getLiquidSphereBlocks();
        Block[] fluids = large ? provider.dimensionInformation.getHugeLiquidSphereFluids() : provider.dimensionInformation.getLiquidSphereFluids();

        for (int cx = -r ; cx <= r ; cx++) {
            for (int cz = -r ; cz <= r ; cz++) {

                Random random = new Random((world.getSeed() + (chunkX+cx)) * 37 + (chunkZ+cz) * 5 + 113);
                random.nextFloat();

                if (random.nextFloat() < .05f) {
                    int x = cx * 16 + random.nextInt(16);
                    int y = 40 + random.nextInt(40);
                    int z = cz * 16 + random.nextInt(16);
                    int radius = random.nextInt(large ? 20 : 6) + (large ? 10 : 4);
                    BlockMeta block = BlockMeta.STONE;
                    if (blocks.length > 1) {
                        block = blocks[random.nextInt(blocks.length)];
                    } else if (blocks.length == 1) {
                        block = blocks[0];
                    }
                    Block fluid = Blocks.water;
                    if (fluids.length > 1) {
                        fluid = fluids[random.nextInt(fluids.length)];
                    } else if (fluids.length == 1) {
                        fluid = fluids[0];
                    }

                    fillSphere(ablock, ameta, x, y, z, radius, block, fluid);
                }
            }
        }
    }

    private void fillSphere(Block[] ablock, byte[] ameta, int centerx, int centery, int centerz, int radius, BlockMeta blockMeta, Block fluid) {
        Block block = blockMeta.getBlock();
        byte meta = blockMeta.getMeta();

        double sqradius = radius * radius;
        double liquidradius = (((double) radius) - 1.5f) * (((double) radius) - 1.5f);

        for (int x = 0 ; x < 16 ; x++) {
            double dxdx = (x-centerx) * (x-centerx);
            for (int z = 0 ; z < 16 ; z++) {
                double dzdz = (z-centerz) * (z-centerz);
                int index = (x * 16 + z) * 256;
                for (int y = centery-radius ; y <= centery+radius ; y++) {
                    double dydy = (y-centery) * (y-centery);
                    double sqdist = dxdx + dydy + dzdz;
                    if (sqdist < liquidradius) {
                        ablock[index + y] = fluid;
                        ameta[index + y] = 0;
                    } else if (sqdist <= sqradius) {
                        ablock[index + y] = block;
                        ameta[index + y] = meta;
                    }
                }
            }
        }
    }

}
