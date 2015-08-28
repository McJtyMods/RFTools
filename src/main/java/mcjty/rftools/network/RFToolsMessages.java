package mcjty.rftools.network;

import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import mcjty.network.PacketHandler;
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
import mcjty.rftools.items.teleportprobe.*;
import mcjty.rftools.playerprops.PacketSendBuffsToClient;
import mcjty.rftools.playerprops.PacketSendBuffsToClientHandler;
import mcjty.rftools.playerprops.PacketSendPreferencesToClient;
import mcjty.rftools.playerprops.PacketSendPreferencesToClientHandler;

public class RFToolsMessages {
    public static SimpleNetworkWrapper INSTANCE;

    public static void registerNetworkMessages(SimpleNetworkWrapper net) {
        INSTANCE = net;

        // Server side
        net.registerMessage(PacketContentsMonitor.class, PacketContentsMonitor.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetAdjacentBlocks.class, PacketGetAdjacentBlocks.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetAdjacentTankBlocks.class, PacketGetAdjacentTankBlocks.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketCrafter.class, PacketCrafter.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetInventory.class, PacketGetInventory.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketSearchItems.class, PacketSearchItems.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetConnectedBlocks.class, PacketGetConnectedBlocks.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetTransmitters.class, PacketGetTransmitters.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetReceivers.class, PacketGetReceivers.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetAllReceivers.class, PacketGetAllReceivers.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetTargets.class, PacketGetTargets.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketSetTarget.class, PacketSetTarget.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketClearTarget.class, PacketClearTarget.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketForceTeleport.class, PacketForceTeleport.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetPlayers.class, PacketGetPlayers.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetSecurityInfo.class, PacketGetSecurityInfo.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetSecurityName.class, PacketGetSecurityName.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetFilters.class, PacketGetFilters.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetDelightingInfo.class, PacketGetDelightingInfo.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetChamberInfo.class, PacketGetChamberInfo.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetDimensionEnergy.class, PacketGetDimensionEnergy.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetDestinationInfo.class, PacketGetDestinationInfo.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetCountInfo.class, PacketGetCountInfo.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketGetScreenData.class, PacketGetScreenData.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketModuleUpdate.class, PacketModuleUpdate.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketCycleStorage.class, PacketCycleStorage.class, PacketHandler.nextID(), Side.SERVER);
        net.registerMessage(PacketCompact.class, PacketCompact.class, PacketHandler.nextID(), Side.SERVER);

        // Client side
        net.registerMessage(PacketInventoryReady.class, PacketInventoryReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketSearchReady.class, PacketSearchReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketAdjacentBlocksReady.class, PacketAdjacentBlocksReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketAdjacentTankBlocksReady.class, PacketAdjacentTankBlocksReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketConnectedBlocksReady.class, PacketConnectedBlocksReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketTransmittersReady.class, PacketTransmittersReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketReceiversReady.class, PacketReceiversReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketAllReceiversReady.class, PacketAllReceiversReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketTargetsReady.class, PacketTargetsReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketPlayersReady.class, PacketPlayersReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketSecurityInfoReady.class, PacketSecurityInfoReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketSecurityNameReady.class, PacketSecurityNameReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketFiltersReady.class, PacketFiltersReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketDelightingInfoReady.class, PacketDelightingInfoReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketChamberInfoReady.class, PacketChamberInfoReady.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketSyncDimensionInfoHandler.class, PacketSyncDimensionInfo.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketCheckDimletConfigHandler.class, PacketCheckDimletConfig.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketReturnEnergyHandler.class, PacketReturnEnergy.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketReturnDestinationInfoHandler.class, PacketReturnDestinationInfo.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketReturnCountInfoHandler.class, PacketReturnCountInfo.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketReturnScreenDataHandler.class, PacketReturnScreenData.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketRegisterDimensionsHandler.class, PacketRegisterDimensions.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketSendBuffsToClientHandler.class, PacketSendBuffsToClient.class, PacketHandler.nextID(), Side.CLIENT);
        net.registerMessage(PacketSendPreferencesToClientHandler.class, PacketSendPreferencesToClient.class, PacketHandler.nextID(), Side.CLIENT);
    }
}
