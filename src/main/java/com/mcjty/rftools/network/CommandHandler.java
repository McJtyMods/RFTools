package com.mcjty.rftools.network;

import java.util.Map;

public interface CommandHandler {
    /// Return true if command was handled correctly. False if not.
    boolean execute(String command, Map<String,Argument> args);
}
