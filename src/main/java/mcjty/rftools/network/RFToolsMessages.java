package mcjty.rftools.network;

import mcjty.lib.network.PacketHandler;
import mcjty.rftools.blocks.crafter.PacketCrafter;
import mcjty.rftools.blocks.screens.network.PacketGetScreenData;
import mcjty.rftools.blocks.screens.network.PacketModuleUpdate;
import mcjty.rftools.blocks.screens.network.PacketReturnScreenData;
import mcjty.rftools.blocks.storage.PacketCompact;
import mcjty.rftools.blocks.storage.PacketCycleStorage;
import mcjty.rftools.blocks.storage.StorageInfoPacketClient;
import mcjty.rftools.blocks.storage.StorageInfoPacketServer;
import mcjty.rftools.blocks.teleporter.*;
import mcjty.rftools.items.teleportprobe.*;
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
        net.registerMessage(PacketGetPlayers.Handler.class, PacketGetPlayers.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetReceivers.Handler.class, PacketGetReceivers.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetAllReceivers.Handler.class, PacketGetAllReceivers.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetTransmitters.Handler.class, PacketGetTransmitters.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetDestinationInfo.Handler.class, PacketGetDestinationInfo.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketClearTarget.Handler.class, PacketClearTarget.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketForceTeleport.Handler.class, PacketForceTeleport.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetTargets.Handler.class, PacketGetTargets.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketSetTarget.Handler.class, PacketSetTarget.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketModuleUpdate.Handler.class, PacketModuleUpdate.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetScreenData.Handler.class, PacketGetScreenData.class, PacketHandler.nextID(), Side.SERVER);

        // Client side
        net.registerMessage(PacketPlayersReady.Handler.class, PacketPlayersReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketTransmittersReady.Handler.class, PacketTransmittersReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketReceiversReady.Handler.class, PacketReceiversReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketAllReceiversReady.Handler.class, PacketAllReceiversReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketReturnDestinationInfo.Handler.class, PacketReturnDestinationInfo.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketTargetsReady.Handler.class, PacketTargetsReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketReturnScreenData.Handler.class, PacketReturnScreenData.class, PacketHandler.nextID(), Side.CLIENT);


        PacketHandler.register(PacketHandler.nextPacketID(), StorageInfoPacketServer.class, StorageInfoPacketClient.class);
    }
}
