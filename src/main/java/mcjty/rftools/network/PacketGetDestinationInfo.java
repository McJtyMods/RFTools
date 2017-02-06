package mcjty.rftools.network;

import io.netty.buffer.ByteBuf;
import mcjty.rftools.blocks.teleporter.TeleportDestinations;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketGetDestinationInfo message, MessageContext ctx) {
            World world = ctx.getServerHandler().player.getEntityWorld();
            TeleportDestinations destinations = TeleportDestinations.getDestinations(world);
            int receiverId = message.receiverId;
            String name = TeleportDestinations.getDestinationName(destinations, receiverId);
            RFToolsMessages.INSTANCE.sendTo(new PacketReturnDestinationInfo(receiverId, name), ctx.getServerHandler().player);
        }

    }

}