package com.mcjty.rftools.blocks.storagemonitor;

import com.mcjty.rftools.network.Argument;
import com.mcjty.rftools.network.PacketRequestListFromClient;
import com.mcjty.varia.Coordinate;

import java.util.List;

public class PacketSearchItems extends PacketRequestListFromClient<Coordinate, PacketSearchItems, PacketSearchReady> {

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
