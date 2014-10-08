package com.mcjty.rftools.blocks.storagemonitor;

import com.mcjty.rftools.network.PacketListFromClient;
import com.mcjty.varia.Coordinate;
import io.netty.buffer.ByteBuf;

import java.util.List;

public class PacketSearchReady extends PacketListFromClient<PacketSearchReady,Coordinate> {

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
