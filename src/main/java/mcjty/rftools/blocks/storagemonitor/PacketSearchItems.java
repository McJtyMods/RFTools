package mcjty.rftools.blocks.storagemonitor;

import mcjty.rftools.network.Argument;
import mcjty.rftools.network.PacketRequestListFromServer;
import mcjty.varia.Coordinate;

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
