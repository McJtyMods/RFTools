package mcjty.rftools.network;

import io.netty.buffer.ByteBuf;

/**
 * Implement this interface for items that you want to request from a server and send back
 * to the client (through the PacketRequestListFromServer/PacketListFromServer system).
 */
public interface ByteBufConverter {
    void toBytes(ByteBuf buf);
}
