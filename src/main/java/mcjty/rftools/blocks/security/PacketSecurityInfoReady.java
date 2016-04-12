package mcjty.rftools.blocks.security;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketSecurityInfoReady implements IMessage {

    private SecurityChannels.SecurityChannel channel;


    @Override
    public void fromBytes(ByteBuf buf) {
        channel = new SecurityChannels.SecurityChannel();
        channel.setName(NetworkTools.readString(buf));
        channel.setWhitelist(buf.readBoolean());
        int size = buf.readInt();
        channel.clearPlayers();
        for (int i = 0 ; i < size ; i++) {
            channel.addPlayer(NetworkTools.readString(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writeString(buf, channel.getName());
        buf.writeBoolean(channel.isWhitelist());
        List<String> players = channel.getPlayers();
        buf.writeInt(players.size());
        for (String player : players) {
            NetworkTools.writeString(buf, player);
        }

    }

    public PacketSecurityInfoReady() {
    }

    public PacketSecurityInfoReady(SecurityChannels.SecurityChannel channel) {
        this.channel = channel;
    }

    public static class Handler implements IMessageHandler<PacketSecurityInfoReady, IMessage> {
        @Override
        public IMessage onMessage(PacketSecurityInfoReady message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketSecurityInfoReady message, MessageContext ctx) {
            GuiSecurityManager.channelFromServer = message.channel;
        }
    }
}
