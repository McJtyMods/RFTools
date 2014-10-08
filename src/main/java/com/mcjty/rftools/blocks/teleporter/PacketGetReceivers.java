package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.rftools.network.PacketRequestListFromServer;

import java.util.List;

public class PacketGetReceivers extends PacketRequestListFromServer<TeleportDestination, PacketGetReceivers, PacketReceiversReady> {

    public PacketGetReceivers() {
    }

    public PacketGetReceivers(int x, int y, int z) {
        super(x, y, z, DialingDeviceTileEntity.CMD_GETRECEIVERS);
    }

    @Override
    protected PacketReceiversReady createMessageToClient(int x, int y, int z, List<TeleportDestination> result) {
        return new PacketReceiversReady(x, y, z, DialingDeviceTileEntity.CLIENTCMD_GETRECEIVERS, result);
    }
}
