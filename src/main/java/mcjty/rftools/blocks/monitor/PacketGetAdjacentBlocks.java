package mcjty.rftools.blocks.monitor;

import mcjty.lib.network.PacketRequestListFromServer;
import mcjty.lib.varia.Coordinate;

import java.util.List;

public class PacketGetAdjacentBlocks extends PacketRequestListFromServer<Coordinate, PacketGetAdjacentBlocks, PacketAdjacentBlocksReady> {

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
