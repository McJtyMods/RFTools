package mcjty.rftools.blocks.teleporter;

import mcjty.lib.network.Argument;
import mcjty.lib.network.PacketRequestListFromServer;
import net.minecraft.util.BlockPos;

import java.util.List;

public class PacketGetReceivers extends PacketRequestListFromServer<TeleportDestinationClientInfo, PacketGetReceivers, PacketReceiversReady> {

    public PacketGetReceivers() {
    }

    public PacketGetReceivers(BlockPos pos, String playerName) {
        super(pos, DialingDeviceTileEntity.CMD_GETRECEIVERS, new Argument("player", playerName));
    }

    @Override
    protected PacketReceiversReady createMessageToClient(BlockPos pos, List<TeleportDestinationClientInfo> result) {
        return new PacketReceiversReady(pos, DialingDeviceTileEntity.CLIENTCMD_GETRECEIVERS, result);
    }
}
