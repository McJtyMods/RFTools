package mcjty.rftools.items.teleportprobe;

import mcjty.lib.varia.WorldTools;
import mcjty.rftools.blocks.teleporter.TeleportDestination;
import mcjty.rftools.blocks.teleporter.TeleportDestinationClientInfo;
import mcjty.rftools.blocks.teleporter.TeleportDestinations;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketGetAllReceivers {

    public void toBytes(PacketBuffer buf) {
    }

    public PacketGetAllReceivers() {
    }

    public PacketGetAllReceivers(PacketBuffer buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity player = ctx.getSender();
            TeleportDestinations destinations = TeleportDestinations.getDestinations(player.getEntityWorld());
            List<TeleportDestinationClientInfo> destinationList = new ArrayList<> (destinations.getValidDestinations(player.getEntityWorld(), null));
            addDimensions(destinationList);
            addRfToolsDimensions(player.getEntityWorld(), destinationList);
            PacketAllReceiversReady msg = new PacketAllReceiversReady(destinationList);
            RFToolsMessages.INSTANCE.sendTo(msg, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.setPacketHandled(true);
    }

    private void addDimensions(List<TeleportDestinationClientInfo> destinationList) {
        for (DimensionType type : DimensionType.getAll()) {
            ServerWorld world = WorldTools.getWorld(type);
            int id = world.getDimension().getType().getId();
            TeleportDestination destination = new TeleportDestination(new BlockPos(0, 70, 0), id);
            destination.setName("Dimension: " + id);
            TeleportDestinationClientInfo teleportDestinationClientInfo = new TeleportDestinationClientInfo(destination);
            String dimName = type.getRegistryName().toString();
            teleportDestinationClientInfo.setDimensionName(dimName);
            destinationList.add(teleportDestinationClientInfo);
        }
    }

    private void addRfToolsDimensions(World world, List<TeleportDestinationClientInfo> destinationList) {
//        RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(world);
//        for (Map.Entry<Integer,DimensionDescriptor> me : dimensionManager.getDimensions().entrySet()) {
//            int id = me.getKey();
//            TeleportDestination destination = new TeleportDestination(new Coordinate(0, 70, 0), id);
//            destination.setName("RfTools Dim: " + id);
//            TeleportDestinationClientInfo teleportDestinationClientInfo = new TeleportDestinationClientInfo(destination);
//            destinationList.add(teleportDestinationClientInfo);
//        }
    }
}
