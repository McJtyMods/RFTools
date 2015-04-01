package mcjty.rftools.blocks.teleporter;

import mcjty.rftools.network.PacketListFromServer;
import io.netty.buffer.ByteBuf;

import java.util.List;

public class PacketReceiversReady extends PacketListFromServer<PacketReceiversReady,TeleportDestinationClientInfo> {

    public PacketReceiversReady() {
    }

    public PacketReceiversReady(int x, int y, int z, String command, List<TeleportDestinationClientInfo> list) {
        super(x, y, z, command, list);
    }

    @Override
    protected TeleportDestinationClientInfo createItem(ByteBuf buf) {
        return new TeleportDestinationClientInfo(buf);
    }
}
