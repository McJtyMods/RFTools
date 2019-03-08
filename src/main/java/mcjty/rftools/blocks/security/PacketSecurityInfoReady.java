package mcjty.rftools.blocks.security;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.List;
import java.util.function.Supplier;

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

    public PacketSecurityInfoReady(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketSecurityInfoReady(SecurityChannels.SecurityChannel channel) {
        this.channel = channel;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            GuiSecurityManager.channelFromServer = channel;
        });
        ctx.setPacketHandled(true);
    }
}
