package mcjty.rftools.items.teleportprobe;

import io.netty.buffer.ByteBuf;
import mcjty.lib.thirteen.Context;
import mcjty.rftools.blocks.teleporter.TeleportDestination;
import mcjty.rftools.blocks.teleporter.TeleportDestinationClientInfo;
import mcjty.rftools.blocks.teleporter.TeleportDestinations;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.servernet.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketGetAllReceivers implements IMessage {

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public PacketGetAllReceivers() {
    }

    public PacketGetAllReceivers(ByteBuf buf) {
        fromBytes(buf);
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            EntityPlayerMP player = ctx.getSender();
            TeleportDestinations destinations = TeleportDestinations.getDestinations(player.getEntityWorld());
            List<TeleportDestinationClientInfo> destinationList = new ArrayList<> (destinations.getValidDestinations(player.getEntityWorld(), null));
            addDimensions(destinationList);
            addRfToolsDimensions(player.getEntityWorld(), destinationList);
            PacketAllReceiversReady msg = new PacketAllReceiversReady(destinationList);
            RFToolsMessages.INSTANCE.sendTo(msg, player);
        });
        ctx.setPacketHandled(true);
    }

    private void addDimensions(List<TeleportDestinationClientInfo> destinationList) {
        ServerWorld[] worlds = DimensionManager.getWorlds();
        for (ServerWorld world : worlds) {
            int id = world.provider.getDimension();
            TeleportDestination destination = new TeleportDestination(new BlockPos(0, 70, 0), id);
            destination.setName("Dimension: " + id);
            TeleportDestinationClientInfo teleportDestinationClientInfo = new TeleportDestinationClientInfo(destination);
            String dimName = world.provider.getDimensionType().getName();
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
