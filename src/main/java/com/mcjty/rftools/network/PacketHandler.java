package com.mcjty.rftools.network;

import com.mcjty.rftools.blocks.crafter.PacketCrafter;
import com.mcjty.rftools.blocks.monitor.PacketAdjacentBlocksReady;
import com.mcjty.rftools.blocks.monitor.PacketGetAdjacentBlocks;
import com.mcjty.rftools.blocks.monitor.PacketRFMonitor;
import com.mcjty.rftools.blocks.screens.PacketModuleUpdate;
import com.mcjty.rftools.blocks.shield.PacketFiltersReady;
import com.mcjty.rftools.blocks.shield.PacketGetFilters;
import com.mcjty.rftools.blocks.storagemonitor.*;
import com.mcjty.rftools.blocks.teleporter.*;
import com.mcjty.rftools.dimension.network.*;
import com.mcjty.rftools.items.devdelight.PacketDelightingInfoReady;
import com.mcjty.rftools.items.devdelight.PacketGetDelightingInfo;
import com.mcjty.rftools.items.netmonitor.PacketConnectedBlocksReady;
import com.mcjty.rftools.items.netmonitor.PacketGetConnectedBlocks;
import com.mcjty.rftools.items.teleportprobe.PacketAllReceiversReady;
import com.mcjty.rftools.items.teleportprobe.PacketForceTeleport;
import com.mcjty.rftools.items.teleportprobe.PacketGetAllReceivers;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class PacketHandler {
    public static SimpleNetworkWrapper INSTANCE;

    private static int ID = 0;

    public static int nextID() {
        return ID++;
    }

    public static void registerMessages() {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("rftools");

        // Server side
        INSTANCE.registerMessage(PacketRFMonitor.class, PacketRFMonitor.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetAdjacentBlocks.class, PacketGetAdjacentBlocks.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketCrafter.class, PacketCrafter.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetInventory.class, PacketGetInventory.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketSearchItems.class, PacketSearchItems.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetConnectedBlocks.class, PacketGetConnectedBlocks.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetTransmitters.class, PacketGetTransmitters.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetReceivers.class, PacketGetReceivers.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketServerCommand.class, PacketServerCommand.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketRequestIntegerFromServer.class, PacketRequestIntegerFromServer.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetAllReceivers.class, PacketGetAllReceivers.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketForceTeleport.class, PacketForceTeleport.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetPlayers.class, PacketGetPlayers.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetFilters.class, PacketGetFilters.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetDelightingInfo.class, PacketGetDelightingInfo.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetDimensionEnergy.class, PacketGetDimensionEnergy.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketModuleUpdate.class, PacketModuleUpdate.class, nextID(), Side.SERVER);

        // Client side
        INSTANCE.registerMessage(PacketInventoryReady.class, PacketInventoryReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketSearchReady.class, PacketSearchReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketAdjacentBlocksReady.class, PacketAdjacentBlocksReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketConnectedBlocksReady.class, PacketConnectedBlocksReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketTransmittersReady.class, PacketTransmittersReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketReceiversReady.class, PacketReceiversReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketIntegerFromServer.class, PacketIntegerFromServer.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketAllReceiversReady.class, PacketAllReceiversReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketPlayersReady.class, PacketPlayersReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketFiltersReady.class, PacketFiltersReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketDelightingInfoReady.class, PacketDelightingInfoReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketSyncDimensionInfoHandler.class, PacketSyncDimensionInfo.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketCheckDimletConfigHandler.class, PacketCheckDimletConfig.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketReturnEnergyHandler.class, PacketReturnEnergy.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketRegisterDimensionsHandler.class, PacketRegisterDimensions.class, nextID(), Side.CLIENT);
    }
}
