package mcjty.rftools.blocks.teleporter;

import mcjty.lib.network.PacketRequestListFromServer;
import net.minecraft.util.BlockPos;

import java.util.List;

public class PacketGetTransmitters extends PacketRequestListFromServer<TransmitterInfo, PacketGetTransmitters, PacketTransmittersReady> {

    public PacketGetTransmitters() {
    }

    public PacketGetTransmitters(BlockPos pos) {
        super(pos, DialingDeviceTileEntity.CMD_GETTRANSMITTERS);
    }

    @Override
    protected PacketTransmittersReady createMessageToClient(BlockPos pos, List<TransmitterInfo> result) {
        return new PacketTransmittersReady(pos, DialingDeviceTileEntity.CLIENTCMD_GETTRANSMITTERS, result);
    }
}
