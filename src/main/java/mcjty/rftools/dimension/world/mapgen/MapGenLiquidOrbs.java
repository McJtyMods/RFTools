package mcjty.rftools.dimension.world.mapgen;

import mcjty.rftools.dimension.world.GenericChunkProvider;
import net.minecraft.block.Block;
import net.minecraft.world.World;

import java.util.Random;

public class MapGenLiquidOrbs {
    private final GenericChunkProvider provider;

    public MapGenLiquidOrbs(GenericChunkProvider provider) {
        this.provider = provider;
    }

    public void generate(World world, int chunkX, int chunkZ, Block[] ablock, byte[] ameta) {

        for (int cx = -1 ; cx <= 1 ; cx++) {
            for (int cz = -1 ; cz <= 1 ; cz++) {

                Random random = new Random((world.getSeed() + (chunkX+cx)) * 37 + (chunkZ+cz) * 5 + 113);
                random.nextFloat();

                if (random.nextFloat() < .05f) {
                    int x = cx * 16 + random.nextInt(16);
                    int y = 40 + random.nextInt(40);
                    int z = cz * 16 + random.nextInt(16);
                    int radius = random.nextInt(6) + 4;
                    fillSphere(ablock, ameta, x, y, z, radius);
                }
            }
        }
    }

    private void fillSphere(Block[] ablock, byte[] ameta, int centerx, int centery, int centerz, int radius) {
        Block block = provider.dimensionInformation.getLiquidSphereBlock().getBlock();
        byte blockMeta = provider.dimensionInformation.getLiquidSphereBlock().getMeta();
        Block fluid = provider.dimensionInformation.getLiquidSphereFluid();

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
                        ameta[index + y] = blockMeta;
                    }
                }
            }
        }
    }

}
