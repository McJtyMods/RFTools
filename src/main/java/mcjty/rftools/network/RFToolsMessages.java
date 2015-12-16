package mcjty.rftools.network;

import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class RFToolsMessages {
    public static SimpleNetworkWrapper INSTANCE;

    public static void registerNetworkMessages(SimpleNetworkWrapper net) {
        INSTANCE = net;

        // Server side

        // Client side
    }
}
