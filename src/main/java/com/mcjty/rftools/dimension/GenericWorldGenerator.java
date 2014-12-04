package com.mcjty.rftools.dimension;

import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.blocks.teleporter.MatterReceiverTileEntity;
import com.mcjty.rftools.blocks.teleporter.TeleportConfiguration;
import com.mcjty.rftools.blocks.teleporter.TeleportDestination;
import com.mcjty.rftools.blocks.teleporter.TeleportDestinations;
import com.mcjty.varia.Coordinate;
import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.init.Blocks;
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

            RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(world);
            DimensionInformation information = dimensionManager.getDimensionInformation(world.provider.dimensionId);

            for (int x = -3 ; x <= 3 ; x++) {
                for (int z = -3 ; z <= 3 ; z++) {
                    if (x == 0 && z == 0) {
                        world.setBlock(x, 70, z, ModBlocks.matterReceiverBlock, 0, 2);
                        MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) world.getTileEntity(x, 70, z);
                        matterReceiverTileEntity.modifyEnergyStored(TeleportConfiguration.RECEIVER_MAXENERGY);
                        matterReceiverTileEntity.setName(information.getName());
                        matterReceiverTileEntity.markDirty();
                    } else {
                        world.setBlock(x, 70, z, Blocks.stone, 0, 2);
                    }
                }
            }

            TeleportDestinations destinations = TeleportDestinations.getDestinations(world);
            TeleportDestination destination = destinations.addDestination(new Coordinate(0, 70, 0), world.provider.dimensionId);
            destination.setName(information.getName());
            destinations.save(world);
        }
    }
}
