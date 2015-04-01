package mcjty.rftools.blocks.teleporter;

import mcjty.rftools.network.PacketRequestListFromServer;

import java.util.List;

public class PacketGetPlayers extends PacketRequestListFromServer<PlayerName, PacketGetPlayers, PacketPlayersReady> {

    public PacketGetPlayers() {
    }

    public PacketGetPlayers(int x, int y, int z) {
        super(x, y, z, MatterTransmitterTileEntity.CMD_GETPLAYERS);
    }

    @Override
    protected PacketPlayersReady createMessageToClient(int x, int y, int z, List<PlayerName> result) {
        return new PacketPlayersReady(x, y, z, MatterTransmitterTileEntity.CLIENTCMD_GETPLAYERS, result);
    }
}
