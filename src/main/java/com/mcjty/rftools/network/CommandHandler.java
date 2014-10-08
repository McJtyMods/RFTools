package com.mcjty.rftools.network;

import java.util.List;
import java.util.Map;

public interface CommandHandler {
    /// Return true if command was handled correctly. False if not.
    boolean execute(String command, Map<String,Argument> args);

    /// Return the result which will be sent back to the client.
    List executeWithResult(String command, Map<String,Argument> args);
}
