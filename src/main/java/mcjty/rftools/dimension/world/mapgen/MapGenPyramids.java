package mcjty.rftools.dimension.world.mapgen;

import mcjty.lib.varia.BlockMeta;
import mcjty.rftools.dimension.world.GenericChunkProvider;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

import java.util.Random;

public class MapGenPyramids {
    private final GenericChunkProvider provider;

    public MapGenPyramids(GenericChunkProvider provider) {
        this.provider = provider;
    }

    public void generate(World world, int chunkX, int chunkZ, Block[] ablock, byte[] ameta) {
        BlockMeta[] blocks = provider.dimensionInformation.getPyramidBlocks();

        Random random = new Random((world.getSeed() + (chunkX)) * 1133 + (chunkZ) * 37 + 77);
        random.nextFloat();

        if (random.nextFloat() < .05f) {
            int x = 8;
            int z = 8;

            int y = getBestY(ablock, 8, 8);
            if (y < 10 || y > 230) {
                return;
            }
            BlockMeta block = BlockMeta.STONE;
            if (blocks.length > 1) {
                block = blocks[random.nextInt(blocks.length)];
            } else if (blocks.length == 1) {
                block = blocks[0];
            }

            for (int i = 7 ; i >= 0 ; i--) {
                for (int dx = -i ; dx <= i-1 ; dx++) {
                    for (int dz = -i ; dz <= i-1 ; dz++) {
                        int index = ((x+dx) * 16 + (z+dz)) * 256;
                        ablock[index+y] = block.getBlock();
                        ameta[index+y] = block.getMeta();
                    }
                }
                y++;
            }
        }
    }

    private int getBestY(Block[] ablock, int x, int z) {
        int y = findTopSolid(ablock, x, z);
        int y1 = findTopSolid(ablock, x - 7, z - 7);
        if (y1 < y) {
            y = y1;
        }
        y1 = findTopSolid(ablock, x + 7, z - 7);
        if (y1 < y) {
            y = y1;
        }
        y1 = findTopSolid(ablock, x - 7, z + 7);
        if (y1 < y) {
            y = y1;
        }
        y1 = findTopSolid(ablock, x + 7, z + 7);
        if (y1 < y) {
            y = y1;
        }
        return y;
    }

    private int findTopSolid(Block[] ablock, int x, int z) {
        int index = (x * 16 + z) * 256;
        int y = 255;
        while (y >= 5 && (ablock[index+y] == null || ablock[index+y].getMaterial() == Material.air)) {
            y--;
        }
        return y;
    }
}
