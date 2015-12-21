package mcjty.rftools.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.blocks.teleporter.TeleportDestination;
import mcjty.rftools.blocks.teleporter.TeleportDestinations;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketGetDestinationInfo implements IMessage {
    private int receiverId;

    @Override
    public void fromBytes(ByteBuf buf) {
        receiverId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(receiverId);
    }

    public PacketGetDestinationInfo() {
    }

    public PacketGetDestinationInfo(int receiverId) {
        this.receiverId = receiverId;
    }

    public static class Handler implements IMessageHandler<PacketGetDestinationInfo, IMessage> {
        @Override
        public IMessage onMessage(PacketGetDestinationInfo message, MessageContext ctx) {
            MinecraftServer.getServer().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketGetDestinationInfo message, MessageContext ctx) {
            World world = ctx.getServerHandler().playerEntity.worldObj;
            TeleportDestinations destinations = TeleportDestinations.getDestinations(world);
            GlobalCoordinate coordinate = destinations.getCoordinateForId(message.receiverId);
            String name;
            if (coordinate == null) {
                name = "?";
            } else {
                TeleportDestination destination = destinations.getDestination(coordinate);
                if (destination == null) {
                    name = "?";
                } else {
                    name = destination.getName();
                    if (name == null || name.isEmpty()) {
                        name = destination.getCoordinate() + " (" + destination.getDimension() + ")";
                    }
                }
            }
            RFToolsMessages.INSTANCE.sendTo(new PacketReturnDestinationInfo(message.receiverId, name), ctx.getServerHandler().playerEntity);
        }

    }
}