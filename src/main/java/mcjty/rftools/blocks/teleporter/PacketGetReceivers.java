package mcjty.rftools.blocks.teleporter;

import mcjty.rftools.network.Argument;
import mcjty.rftools.network.PacketRequestListFromServer;

import java.util.List;

public class PacketGetReceivers extends PacketRequestListFromServer<TeleportDestinationClientInfo, PacketGetReceivers, PacketReceiversReady> {

    public PacketGetReceivers() {
    }

    public PacketGetReceivers(int x, int y, int z, String playerName) {
        super(x, y, z, DialingDeviceTileEntity.CMD_GETRECEIVERS, new Argument("player", playerName));
    }

    @Override
    protected PacketReceiversReady createMessageToClient(int x, int y, int z, List<TeleportDestinationClientInfo> result) {
        return new PacketReceiversReady(x, y, z, DialingDeviceTileEntity.CLIENTCMD_GETRECEIVERS, result);
    }
}
