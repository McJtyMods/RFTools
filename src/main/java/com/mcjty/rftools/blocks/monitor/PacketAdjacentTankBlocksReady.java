package com.mcjty.rftools.blocks.monitor;

import com.mcjty.rftools.network.PacketListFromServer;
import com.mcjty.varia.Coordinate;
import io.netty.buffer.ByteBuf;

import java.util.List;

public class PacketAdjacentTankBlocksReady extends PacketListFromServer<PacketAdjacentTankBlocksReady,Coordinate> {

    public PacketAdjacentTankBlocksReady() {
    }

    public PacketAdjacentTankBlocksReady(int x, int y, int z, String command, List<Coordinate> list) {
        super(x, y, z, command, list);
    }

    @Override
    protected Coordinate createItem(ByteBuf buf) {
        return new Coordinate(buf);
    }
}
