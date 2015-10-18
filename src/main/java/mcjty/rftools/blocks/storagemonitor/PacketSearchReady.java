package mcjty.rftools.blocks.storagemonitor;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.PacketListFromServer;
import mcjty.lib.varia.Coordinate;

import java.util.List;

public class PacketSearchReady extends PacketListFromServer<PacketSearchReady,Coordinate> {

    public PacketSearchReady() {
    }

    public PacketSearchReady(int x, int y, int z, String command, List<Coordinate> list) {
        super(x, y, z, command, list);
    }

    @Override
    protected Coordinate createItem(ByteBuf buf) {
        return new Coordinate(buf);
    }
}
