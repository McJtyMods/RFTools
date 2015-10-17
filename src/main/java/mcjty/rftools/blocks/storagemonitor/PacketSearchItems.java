package mcjty.rftools.blocks.storagemonitor;

import mcjty.lib.network.Argument;
import mcjty.lib.network.PacketRequestListFromServer;
import mcjty.lib.varia.Coordinate;

import java.util.List;

public class PacketSearchItems extends PacketRequestListFromServer<Coordinate, PacketSearchItems, PacketSearchReady> {

    public PacketSearchItems() {
    }

    public PacketSearchItems(int x, int y, int z, String search) {
        super(x, y, z, StorageScannerTileEntity.CMD_STARTSEARCH, new Argument("search", search));
    }

    @Override
    protected PacketSearchReady createMessageToClient(int x, int y, int z, List<Coordinate> result) {
        return new PacketSearchReady(x, y, z, StorageScannerTileEntity.CLIENTCMD_SEARCHREADY, result);
    }
}
