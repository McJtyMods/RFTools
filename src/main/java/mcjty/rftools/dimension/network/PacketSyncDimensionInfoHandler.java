package mcjty.rftools.dimension.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * Sync RfToolsDimensionManager data from server to client.
 */
public class PacketSyncDimensionInfoHandler implements IMessageHandler<PacketSyncDimensionInfo, IMessage> {
    @Override
    public IMessage onMessage(PacketSyncDimensionInfo message, MessageContext ctx) {
        SyncDimensionInfoHelper.syncDimensionManagerFromServer(message);
        return null;
    }

}
