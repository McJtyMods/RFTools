package com.mcjty.rftools.network;

import java.util.List;
import java.util.Map;

/**
 * Implement this interface if you want to receive server-side messages (typically sent from a packet that
 * implements PacketRequestListFromServer or PacketServerCommand).
 */
public interface CommandHandler {
    /// Return true if command was handled correctly. False if not.
    boolean execute(String command, Map<String,Argument> args);

    /// Return the result which will be sent back to the client.
    List executeWithResult(String command, Map<String,Argument> args);
}
