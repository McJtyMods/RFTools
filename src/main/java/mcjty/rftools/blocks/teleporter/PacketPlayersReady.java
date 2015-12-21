package mcjty.rftools.blocks.teleporter;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.PacketListFromServer;
import net.minecraft.util.BlockPos;

import java.util.List;

public class PacketPlayersReady extends PacketListFromServer<PacketPlayersReady,PlayerName> {

    public PacketPlayersReady() {
    }

    public PacketPlayersReady(BlockPos pos, String command, List<PlayerName> list) {
        super(pos, command, list);
    }

    @Override
    protected PlayerName createItem(ByteBuf buf) {
        return new PlayerName(buf);
    }
}
