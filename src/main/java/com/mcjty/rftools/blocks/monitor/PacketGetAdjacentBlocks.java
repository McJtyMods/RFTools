package com.mcjty.rftools.blocks.monitor;

import com.mcjty.rftools.network.PacketRequestListFromClient;
import com.mcjty.varia.Coordinate;

import java.util.List;

public class PacketGetAdjacentBlocks extends PacketRequestListFromClient<Coordinate, PacketGetAdjacentBlocks, PacketAdjacentBlocksReady> {

    public PacketGetAdjacentBlocks() {
    }

    public PacketGetAdjacentBlocks(int x, int y, int z) {
        super(x, y, z, RFMonitorBlockTileEntity.CMD_GETADJACENTBLOCKS);
    }

    @Override
    protected PacketAdjacentBlocksReady createMessageToClient(int x, int y, int z, List<Coordinate> result) {
        return new PacketAdjacentBlocksReady(x, y, z, RFMonitorBlockTileEntity.CLIENTCMD_ADJACENTBLOCKSREADY, result);
    }
}
