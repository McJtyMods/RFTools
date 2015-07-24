package mcjty.rftools.blocks.monitor;

import mcjty.network.PacketRequestListFromServer;
import mcjty.varia.Coordinate;

import java.util.List;

public class PacketGetAdjacentTankBlocks extends PacketRequestListFromServer<Coordinate, PacketGetAdjacentTankBlocks, PacketAdjacentTankBlocksReady> {

    public PacketGetAdjacentTankBlocks() {
    }

    public PacketGetAdjacentTankBlocks(int x, int y, int z) {
        super(x, y, z, LiquidMonitorBlockTileEntity.CMD_GETADJACENTBLOCKS);
    }

    @Override
    protected PacketAdjacentTankBlocksReady createMessageToClient(int x, int y, int z, List<Coordinate> result) {
        return new PacketAdjacentTankBlocksReady(x, y, z, LiquidMonitorBlockTileEntity.CLIENTCMD_ADJACENTBLOCKSREADY, result);
    }
}
