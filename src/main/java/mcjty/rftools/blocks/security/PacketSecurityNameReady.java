package mcjty.rftools.blocks.security;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSecurityNameReady implements IMessage {

    private String name;


    @Override
    public void fromBytes(ByteBuf buf) {
        name = NetworkTools.readString(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writeString(buf, name);
    }

    public PacketSecurityNameReady() {
    }

    public PacketSecurityNameReady(SecurityChannels.SecurityChannel channel) {
        this.name = channel.getName();
    }

    public static class Handler implements IMessageHandler<PacketSecurityNameReady, IMessage> {
        @Override
        public IMessage onMessage(PacketSecurityNameReady message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        public void handle(PacketSecurityNameReady message, MessageContext ctx) {
            SecurityCardItem.channelNameFromServer = message.name;
        }
    }
}
