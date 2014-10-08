package com.mcjty.rftools.network;

import java.util.List;

/**
 * Implement this interface if you want to receive client-side messages (typically sent from a packet that
 * implements PacketListFromServer).
 */
public interface ClientCommandHandler {
    /// Return true if command was handled correctly. False if not.
    boolean execute(String command, List list);
}
