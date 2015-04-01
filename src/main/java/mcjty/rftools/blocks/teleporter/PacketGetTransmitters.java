package mcjty.rftools.blocks.teleporter;

import mcjty.rftools.network.PacketRequestListFromServer;

import java.util.List;

public class PacketGetTransmitters extends PacketRequestListFromServer<TransmitterInfo, PacketGetTransmitters, PacketTransmittersReady> {

    public PacketGetTransmitters() {
    }

    public PacketGetTransmitters(int x, int y, int z) {
        super(x, y, z, DialingDeviceTileEntity.CMD_GETTRANSMITTERS);
    }

    @Override
    protected PacketTransmittersReady createMessageToClient(int x, int y, int z, List<TransmitterInfo> result) {
        return new PacketTransmittersReady(x, y, z, DialingDeviceTileEntity.CLIENTCMD_GETTRANSMITTERS, result);
    }
}
