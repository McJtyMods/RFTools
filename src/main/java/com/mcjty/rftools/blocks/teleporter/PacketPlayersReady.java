package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.rftools.network.PacketListFromServer;
import io.netty.buffer.ByteBuf;

import java.util.List;

public class PacketPlayersReady extends PacketListFromServer<PacketPlayersReady,PlayerName> {

    public PacketPlayersReady() {
    }

    public PacketPlayersReady(int x, int y, int z, String command, List<PlayerName> list) {
        super(x, y, z, command, list);
    }

    @Override
    protected PlayerName createItem(ByteBuf buf) {
        return new PlayerName(buf);
    }
}
