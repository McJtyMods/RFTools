package mcjty.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class PacketHandler {
    public static SimpleNetworkWrapper INSTANCE;

    private static int ID = 0;

    public static int nextID() {
        return ID++;
    }

    public static void registerMessages(String channelName) {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(channelName);

        // Server side
        INSTANCE.registerMessage(PacketSetGuiStyle.class, PacketSetGuiStyle.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketServerCommand.class, PacketServerCommand.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketRequestIntegerFromServer.class, PacketRequestIntegerFromServer.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketUpdateNBTItem.class, PacketUpdateNBTItem.class, nextID(), Side.SERVER);

        // Client side
        INSTANCE.registerMessage(PacketIntegerFromServer.class, PacketIntegerFromServer.class, nextID(), Side.CLIENT);
    }
}
