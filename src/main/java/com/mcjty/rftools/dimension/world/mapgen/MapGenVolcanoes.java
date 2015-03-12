package com.mcjty.rftools.dimension.world.mapgen;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.dimension.world.GenericChunkProvider;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
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
            if (random.nextFloat() < .1f) {
                int x = random.nextInt(16);
                int z = random.nextInt(16);
                int index = (x * 16 + z) * 256;
                int cntsolid = 0;
                for (int y = world.getHeight()-5 ; y >= 4 ; y--) {
                    if (ablock[index+y] != null && ablock[index+y].getMaterial() != Material.air) {
                        cntsolid++;
                        if (cntsolid >= 5) {
                            ablock[index + y] = ModBlocks.volcanicCoreBlock;
                            ameta[index + y] = 0;
                            RFTools.log("Spawned volcano block at " + (chunkX * 16 + x) + "," + y + "," + (chunkZ * 16 + z));
                            return;
                        }
                    } else {
                        cntsolid = 0;
                    }
                }
            }
        }
    }
}
