package com.mcjty.rftools.network;

import com.mcjty.rftools.blocks.crafter.PacketCrafter;
import com.mcjty.rftools.blocks.monitor.PacketRFMonitor;
import com.mcjty.rftools.blocks.storagemonitor.PacketGetInventory;
import com.mcjty.rftools.blocks.storagemonitor.PacketInventoryReady;
import com.mcjty.rftools.blocks.storagemonitor.PacketSetRadius;
import com.mcjty.rftools.blocks.storagemonitor.PacketStartScan;
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
        INSTANCE.registerMessage(PacketRFMonitor.class, PacketRFMonitor.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketCrafter.class, PacketCrafter.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketStartScan.class, PacketStartScan.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketSetRadius.class, PacketSetRadius.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketGetInventory.class, PacketGetInventory.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketInventoryReady.class, PacketInventoryReady.class, nextID(), Side.CLIENT);
    }
}
