package mcjty.rftools.blocks.security;

import mcjty.lib.network.NetworkTools;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class PacketSecurityInfoReady {

    private SecurityChannels.SecurityChannel channel;

    public void toBytes(PacketBuffer buf) {
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

    public PacketSecurityInfoReady(PacketBuffer buf) {
        channel = new SecurityChannels.SecurityChannel();
        channel.setName(NetworkTools.readString(buf));
        channel.setWhitelist(buf.readBoolean());
        int size = buf.readInt();
        channel.clearPlayers();
        for (int i = 0 ; i < size ; i++) {
            channel.addPlayer(NetworkTools.readString(buf));
        }
    }

    public PacketSecurityInfoReady(SecurityChannels.SecurityChannel channel) {
        this.channel = channel;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            GuiSecurityManager.channelFromServer = channel;
        });
        ctx.setPacketHandled(true);
    }
}
