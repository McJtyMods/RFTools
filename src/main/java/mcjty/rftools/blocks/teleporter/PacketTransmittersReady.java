package mcjty.rftools.blocks.teleporter;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.PacketListFromServer;
import net.minecraft.util.BlockPos;

import java.util.List;

public class PacketTransmittersReady extends PacketListFromServer<PacketTransmittersReady,TransmitterInfo> {

    public PacketTransmittersReady() {
    }

    public PacketTransmittersReady(BlockPos pos, String command, List<TransmitterInfo> list) {
        super(pos, command, list);
    }

    @Override
    protected TransmitterInfo createItem(ByteBuf buf) {
        return new TransmitterInfo(buf);
    }
}
