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

    /// Return the result which will be sent back to the client. Returns nulli f command was not handled.
    List executeWithResultList(String command, Map<String, Argument> args);

    /// Return a numeric result which will be sent back to the client. Returns null if command was not handled.
    Integer executeWithResultInteger(String command, Map<String, Argument> args);
}
