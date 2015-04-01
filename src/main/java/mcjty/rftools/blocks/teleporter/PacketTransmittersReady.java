package mcjty.rftools.blocks.teleporter;

import mcjty.rftools.network.PacketListFromServer;
import io.netty.buffer.ByteBuf;

import java.util.List;

public class PacketTransmittersReady extends PacketListFromServer<PacketTransmittersReady,TransmitterInfo> {

    public PacketTransmittersReady() {
    }

    public PacketTransmittersReady(int x, int y, int z, String command, List<TransmitterInfo> list) {
        super(x, y, z, command, list);
    }

    @Override
    protected TransmitterInfo createItem(ByteBuf buf) {
        return new TransmitterInfo(buf);
    }
}
