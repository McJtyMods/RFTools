package com.mcjty.rftools.network;

import java.util.List;

public interface ClientCommandHandler {
    /// Return true if command was handled correctly. False if not.
    boolean execute(String command, List list);
}
