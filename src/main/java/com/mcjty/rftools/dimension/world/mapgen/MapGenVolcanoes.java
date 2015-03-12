package com.mcjty.rftools.dimension.world.mapgen;

import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.dimension.world.GenericChunkProvider;
import net.minecraft.block.Block;
import net.minecraft.world.World;

import java.util.Random;

public class MapGenVolcanoes {
    private final GenericChunkProvider provider;

    public MapGenVolcanoes(GenericChunkProvider provider) {
        this.provider = provider;
    }

    public void generate(World world, int chunkX, int chunkZ, Block[] ablock, byte[] ameta) {
        if (Math.abs(chunkX) > 2 && Math.abs(chunkZ) > 2) {
            Random random = new Random((world.getSeed() + (chunkX)) * 31 + (chunkZ) * 113 + 77);
            random.nextFloat();
            if (random.nextFloat() < .05f) {
                int x = random.nextInt(16);
                int z = random.nextInt(16);
                int cx = chunkX * 16 + x;
                int cz = chunkZ * 16 + z;
                int y = world.getTopSolidOrLiquidBlock(cx, cz);
                y -= 5;
                if (y > 3 && y < world.getHeight() - 5) {
                    int index = (x * 16 + z) * 256;
                    ablock[index + y] = ModBlocks.volcanicCoreBlock;
                    ameta[index + y] = 0;
                }
            }
        }
    }
}
