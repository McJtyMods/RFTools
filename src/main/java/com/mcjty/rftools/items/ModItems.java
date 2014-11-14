package com.mcjty.rftools.items;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.items.devdelight.DevelopersDelightItem;
import com.mcjty.rftools.items.dimlets.DimletItems;
import com.mcjty.rftools.items.dimlets.EmptyDimentionTab;
import com.mcjty.rftools.items.dimlets.RealizedDimentionTab;
import com.mcjty.rftools.items.dimlets.UnknownDimlet;
import com.mcjty.rftools.items.manual.RFToolsManualItem;
import com.mcjty.rftools.items.netmonitor.NetworkMonitorItem;
import com.mcjty.rftools.items.teleportprobe.TeleportProbeItem;
import cpw.mods.fml.common.registry.GameRegistry;

public final class ModItems {
    public static NetworkMonitorItem networkMonitorItem;
    public static TeleportProbeItem teleportProbeItem;
    public static RFToolsManualItem rfToolsManualItem;
    public static DevelopersDelightItem developersDelightItem;
    public static UnknownDimlet unknownDimlet;
    public static EmptyDimentionTab emptyDimentionTab;
    public static RealizedDimentionTab realizedDimentionTab;

    public static void init() {
        networkMonitorItem = new NetworkMonitorItem();
        networkMonitorItem.setUnlocalizedName("NetworkMonitor");
        networkMonitorItem.setCreativeTab(RFTools.tabRfTools);
        networkMonitorItem.setTextureName(RFTools.MODID + ":networkMonitorItem");
        GameRegistry.registerItem(networkMonitorItem, "networkMonitorItem");

        teleportProbeItem = new TeleportProbeItem();
        teleportProbeItem.setUnlocalizedName("TeleportProbe");
        teleportProbeItem.setCreativeTab(RFTools.tabRfTools);
        teleportProbeItem.setTextureName(RFTools.MODID + ":teleportProbeItem");
        GameRegistry.registerItem(teleportProbeItem, "teleportProbeItem");

        rfToolsManualItem = new RFToolsManualItem();
        rfToolsManualItem.setUnlocalizedName("RFToolsManual");
        rfToolsManualItem.setCreativeTab(RFTools.tabRfTools);
        rfToolsManualItem.setTextureName(RFTools.MODID + ":rftoolsManual");
        GameRegistry.registerItem(rfToolsManualItem, "rfToolsManualItem");

        developersDelightItem = new DevelopersDelightItem();
        developersDelightItem.setUnlocalizedName("DevelopersDelight");
        developersDelightItem.setCreativeTab(RFTools.tabRfTools);
        developersDelightItem.setTextureName(RFTools.MODID + ":developersDelightItem");
        GameRegistry.registerItem(developersDelightItem, "developersDelightItem");

        unknownDimlet = new UnknownDimlet();
        unknownDimlet.setUnlocalizedName("UnknownDimlet");
        unknownDimlet.setCreativeTab(RFTools.tabRfToolsDimlets);
        unknownDimlet.setTextureName(RFTools.MODID + ":unknownDimletItem");
        GameRegistry.registerItem(unknownDimlet, "unknownDimlet");

        emptyDimentionTab = new EmptyDimentionTab();
        emptyDimentionTab.setUnlocalizedName("EmptyDimensionTab");
        emptyDimentionTab.setCreativeTab(RFTools.tabRfTools);
        emptyDimentionTab.setTextureName(RFTools.MODID + ":emptyDimensionTabItem");
        GameRegistry.registerItem(emptyDimentionTab, "emptyDimensionTab");

        realizedDimentionTab = new RealizedDimentionTab();
        realizedDimentionTab.setUnlocalizedName("RealizedDimensionTab");
        realizedDimentionTab.setCreativeTab(RFTools.tabRfTools);
        realizedDimentionTab.setTextureName(RFTools.MODID + ":realizedDimensionTabItem");
        GameRegistry.registerItem(realizedDimentionTab, "realizedDimensionTab");

        DimletItems.init();
    }
}
