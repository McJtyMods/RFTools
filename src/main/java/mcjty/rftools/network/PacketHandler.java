package mcjty.rftools.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import mcjty.container.PacketSetGuiStyle;
import mcjty.rftools.blocks.crafter.PacketCrafter;
import mcjty.rftools.blocks.monitor.*;
import mcjty.rftools.blocks.screens.network.PacketGetScreenData;
import mcjty.rftools.blocks.screens.network.PacketModuleUpdate;
import mcjty.rftools.blocks.screens.network.PacketReturnScreenData;
import mcjty.rftools.blocks.screens.network.PacketReturnScreenDataHandler;
import mcjty.rftools.blocks.security.PacketGetSecurityInfo;
import mcjty.rftools.blocks.security.PacketGetSecurityName;
import mcjty.rftools.blocks.security.PacketSecurityInfoReady;
import mcjty.rftools.blocks.security.PacketSecurityNameReady;
import mcjty.rftools.blocks.shield.PacketFiltersReady;
import mcjty.rftools.blocks.shield.PacketGetFilters;
import mcjty.rftools.blocks.spaceprojector.PacketChamberInfoReady;
import mcjty.rftools.blocks.spaceprojector.PacketGetChamberInfo;
import mcjty.rftools.blocks.storage.*;
import mcjty.rftools.blocks.storagemonitor.PacketGetInventory;
import mcjty.rftools.blocks.storagemonitor.PacketInventoryReady;
import mcjty.rftools.blocks.storagemonitor.PacketSearchItems;
import mcjty.rftools.blocks.storagemonitor.PacketSearchReady;
import mcjty.rftools.blocks.teleporter.*;
import mcjty.rftools.dimension.network.*;
import mcjty.rftools.items.devdelight.PacketDelightingInfoReady;
import mcjty.rftools.items.devdelight.PacketGetDelightingInfo;
import mcjty.rftools.items.netmonitor.PacketConnectedBlocksReady;
import mcjty.rftools.items.netmonitor.PacketGetConnectedBlocks;
import mcjty.rftools.items.teleportprobe.PacketAllReceiversReady;
import mcjty.rftools.items.teleportprobe.PacketForceTeleport;
import mcjty.rftools.items.teleportprobe.PacketGetAllReceivers;
import mcjty.rftools.playerprops.PacketSendBuffsToClient;
import mcjty.rftools.playerprops.PacketSendBuffsToClientHandler;
import mcjty.rftools.playerprops.PacketSendPreferencesToClient;
import mcjty.rftools.playerprops.PacketSendPreferencesToClientHandler;

public class PacketHandler {
    public static SimpleNetworkWrapper INSTANCE;

    private static int ID = 0;

    public static int nextID() {
        return ID++;
    }

    public static void registerMessages() {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("rftools");

        // Server side
        INSTANCE.registerMessage(PacketContentsMonitor.class, PacketContentsMonitor.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetAdjacentBlocks.class, PacketGetAdjacentBlocks.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetAdjacentTankBlocks.class, PacketGetAdjacentTankBlocks.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketCrafter.class, PacketCrafter.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetInventory.class, PacketGetInventory.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketSearchItems.class, PacketSearchItems.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetConnectedBlocks.class, PacketGetConnectedBlocks.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetTransmitters.class, PacketGetTransmitters.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetReceivers.class, PacketGetReceivers.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketServerCommand.class, PacketServerCommand.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketSetGuiStyle.class, PacketSetGuiStyle.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketRequestIntegerFromServer.class, PacketRequestIntegerFromServer.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetAllReceivers.class, PacketGetAllReceivers.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketForceTeleport.class, PacketForceTeleport.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetPlayers.class, PacketGetPlayers.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetSecurityInfo.class, PacketGetSecurityInfo.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetSecurityName.class, PacketGetSecurityName.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetFilters.class, PacketGetFilters.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetDelightingInfo.class, PacketGetDelightingInfo.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetChamberInfo.class, PacketGetChamberInfo.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetDimensionEnergy.class, PacketGetDimensionEnergy.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetDestinationInfo.class, PacketGetDestinationInfo.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetCountInfo.class, PacketGetCountInfo.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetScreenData.class, PacketGetScreenData.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketModuleUpdate.class, PacketModuleUpdate.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketUpdateNBTItem.class, PacketUpdateNBTItem.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketCycleStorage.class, PacketCycleStorage.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketCompact.class, PacketCompact.class, nextID(), Side.SERVER);

        // Client side
        INSTANCE.registerMessage(PacketInventoryReady.class, PacketInventoryReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketSearchReady.class, PacketSearchReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketAdjacentBlocksReady.class, PacketAdjacentBlocksReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketAdjacentTankBlocksReady.class, PacketAdjacentTankBlocksReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketConnectedBlocksReady.class, PacketConnectedBlocksReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketTransmittersReady.class, PacketTransmittersReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketReceiversReady.class, PacketReceiversReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketIntegerFromServer.class, PacketIntegerFromServer.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketAllReceiversReady.class, PacketAllReceiversReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketPlayersReady.class, PacketPlayersReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketSecurityInfoReady.class, PacketSecurityInfoReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketSecurityNameReady.class, PacketSecurityNameReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketFiltersReady.class, PacketFiltersReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketDelightingInfoReady.class, PacketDelightingInfoReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketChamberInfoReady.class, PacketChamberInfoReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketSyncDimensionInfoHandler.class, PacketSyncDimensionInfo.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketCheckDimletConfigHandler.class, PacketCheckDimletConfig.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketReturnEnergyHandler.class, PacketReturnEnergy.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketReturnDestinationInfoHandler.class, PacketReturnDestinationInfo.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketReturnCountInfoHandler.class, PacketReturnCountInfo.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketReturnScreenDataHandler.class, PacketReturnScreenData.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketRegisterDimensionsHandler.class, PacketRegisterDimensions.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketSendBuffsToClientHandler.class, PacketSendBuffsToClient.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketSendPreferencesToClientHandler.class, PacketSendPreferencesToClient.class, nextID(), Side.CLIENT);
    }
}
