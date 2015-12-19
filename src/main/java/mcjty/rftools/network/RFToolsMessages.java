package mcjty.rftools.network;

import mcjty.lib.network.PacketHandler;
import mcjty.rftools.blocks.crafter.PacketCrafter;
import mcjty.rftools.blocks.storage.PacketCompact;
import mcjty.rftools.blocks.storage.PacketCycleStorage;
import mcjty.rftools.blocks.storage.StorageInfoPacketClient;
import mcjty.rftools.blocks.storage.StorageInfoPacketServer;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class RFToolsMessages {
    public static SimpleNetworkWrapper INSTANCE;

    public static void registerNetworkMessages(SimpleNetworkWrapper net) {
        INSTANCE = net;

        // Server side
        net.registerMessage(PacketCrafter.Handler.class, PacketCrafter.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketCompact.Handler.class, PacketCompact.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketCycleStorage.Handler.class, PacketCycleStorage.class, PacketHandler.nextID(), Side.SERVER);

        // Client side

        PacketHandler.register(PacketHandler.nextPacketID(), StorageInfoPacketServer.class, StorageInfoPacketClient.class);
    }
}
