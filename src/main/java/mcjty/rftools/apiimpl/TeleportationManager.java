package mcjty.rftools.apiimpl;

import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.api.teleportation.ITeleportationManager;
import mcjty.rftools.blocks.teleporter.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TeleportationManager implements ITeleportationManager {

    @Override
    public String getReceiverName(World world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() == TeleporterSetup.matterReceiverBlock) {
            MatterReceiverTileEntity te = (MatterReceiverTileEntity) world.getTileEntity(pos);
            return te.getName();
        } else {
            return null;
        }
    }

    @Override
    public boolean createReceiver(World world, BlockPos pos, String name, int power) {
        world.setBlockState(pos, TeleporterSetup.matterReceiverBlock.getDefaultState(), 2);
        MatterReceiverTileEntity te = (MatterReceiverTileEntity) world.getTileEntity(pos);
        if (power == -1) {
            te.modifyEnergyStored(TeleportConfiguration.RECEIVER_MAXENERGY.get());
        } else {
            te.modifyEnergyStored(Math.min(power, TeleportConfiguration.RECEIVER_MAXENERGY.get()));
        }
        te.setName(name);
        te.markDirty();
        registerReceiver(world, pos, name);
        return true;
    }

    private void registerReceiver(World world, BlockPos pos, String name) {
        TeleportDestinations destinations = TeleportDestinations.getDestinations(world);
        GlobalCoordinate gc = new GlobalCoordinate(pos, world.getDimension().getType().getId());
        TeleportDestination destination = destinations.addDestination(gc);
        destination.setName(name);
        destinations.save();
    }

    @Override
    public void teleportPlayer(PlayerEntity player, int dimension, BlockPos location) {
        mcjty.lib.varia.TeleportationTools.teleportToDimension(player, dimension, location.getX(), location.getY(), location.getZ());
    }

    @Override
    public void removeReceiverDestinations(World world, int dim) {
        TeleportDestinations destinations = TeleportDestinations.getDestinations(world);
        destinations.removeDestinationsInDimension(dim);
        destinations.save();
    }
}
