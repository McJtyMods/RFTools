package com.mcjty.rftools.dimension;

import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.blocks.teleporter.TeleportDestination;
import com.mcjty.rftools.blocks.teleporter.TeleportDestinations;
import com.mcjty.varia.Coordinate;
import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.Random;

public class GenericWorldGenerator implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        if (chunkX == 0 && chunkZ == 0) {
            RfToolsDimensionManager manager = RfToolsDimensionManager.getDimensionManager(world);
            if (manager.getDimensionDescriptor(world.provider.dimensionId) == null) {
                return; // Not one of RFTools dimensions
            }
            System.out.println("com.mcjty.rftools.dimension.GenericWorldGenerator.generate");
            world.setBlock(0, 70, 0, ModBlocks.matterReceiverBlock, 0, 2);

            TeleportDestinations destinations = TeleportDestinations.getDestinations(world);
            TeleportDestination destination = destinations.addDestination(new Coordinate(0, 70, 0), world.provider.dimensionId);
            destination.setName(world.provider.getDimensionName());
            destinations.save(world);
        }
    }
}
