package mcjty.rftools.blocks.teleporter;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.PacketListFromServer;
import net.minecraft.util.BlockPos;

import java.util.List;

public class PacketReceiversReady extends PacketListFromServer<PacketReceiversReady,TeleportDestinationClientInfo> {

    public PacketReceiversReady() {
    }

    public PacketReceiversReady(BlockPos pos, String command, List<TeleportDestinationClientInfo> list) {
        super(pos, command, list);
    }

    @Override
    protected TeleportDestinationClientInfo createItem(ByteBuf buf) {
        return new TeleportDestinationClientInfo(buf);
    }
}
