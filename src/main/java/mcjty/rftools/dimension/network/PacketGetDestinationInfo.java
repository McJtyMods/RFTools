package mcjty.rftools.dimension.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mcjty.rftools.blocks.teleporter.TeleportDestination;
import mcjty.rftools.blocks.teleporter.TeleportDestinations;
import mcjty.varia.GlobalCoordinate;
import net.minecraft.world.World;

public class PacketGetDestinationInfo implements IMessage,IMessageHandler<PacketGetDestinationInfo, PacketReturnDestinationInfo> {
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

    @Override
    public PacketReturnDestinationInfo onMessage(PacketGetDestinationInfo message, MessageContext ctx) {
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
        return new PacketReturnDestinationInfo(message.receiverId, name);
    }

}