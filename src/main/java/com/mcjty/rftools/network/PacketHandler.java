package com.mcjty.rftools.network;

import com.mcjty.rftools.blocks.crafter.PacketCrafter;
import com.mcjty.rftools.blocks.crafter.PacketCrafterMode;
import com.mcjty.rftools.blocks.monitor.PacketAdjacentBlocksReady;
import com.mcjty.rftools.blocks.monitor.PacketGetAdjacentBlocks;
import com.mcjty.rftools.blocks.monitor.PacketRFMonitor;
import com.mcjty.rftools.blocks.relay.PacketRelaySettings;
import com.mcjty.rftools.blocks.storagemonitor.*;
import com.mcjty.rftools.blocks.teleporter.*;
import com.mcjty.rftools.items.netmonitor.PacketConnectedBlocksReady;
import com.mcjty.rftools.items.netmonitor.PacketGetConnectedBlocks;
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
        INSTANCE.registerMessage(PacketCrafterMode.class, PacketCrafterMode.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketStartScan.class, PacketStartScan.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketSetRadius.class, PacketSetRadius.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetInventory.class, PacketGetInventory.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketSearchItems.class, PacketSearchItems.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketRelaySettings.class, PacketRelaySettings.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetConnectedBlocks.class, PacketGetConnectedBlocks.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketSetTransmitterName.class, PacketSetTransmitterName.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketSetReceiverName.class, PacketSetReceiverName.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetTransmitters.class, PacketGetTransmitters.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetReceivers.class, PacketGetReceivers.class, nextID(), Side.SERVER);

        // Client side
        INSTANCE.registerMessage(PacketInventoryReady.class, PacketInventoryReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketSearchReady.class, PacketSearchReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketAdjacentBlocksReady.class, PacketAdjacentBlocksReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketConnectedBlocksReady.class, PacketConnectedBlocksReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketTransmittersReady.class, PacketTransmittersReady.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketReceiversReady.class, PacketReceiversReady.class, nextID(), Side.CLIENT);
    }
}
