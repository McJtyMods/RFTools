package com.mcjty.rftools.items;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.items.devdelight.DevelopersDelightItem;
import com.mcjty.rftools.items.dimensionmonitor.DimensionMonitorItem;
import com.mcjty.rftools.items.dimlets.*;
import com.mcjty.rftools.items.manual.RFToolsManualDimensionItem;
import com.mcjty.rftools.items.manual.RFToolsManualItem;
import com.mcjty.rftools.items.netmonitor.NetworkMonitorItem;
import com.mcjty.rftools.items.screenmodules.*;
import com.mcjty.rftools.items.teleportprobe.TeleportProbeItem;
import cpw.mods.fml.common.registry.GameRegistry;

public final class ModItems {
    public static NetworkMonitorItem networkMonitorItem;
    public static TeleportProbeItem teleportProbeItem;
    public static RFToolsManualItem rfToolsManualItem;
    public static RFToolsManualDimensionItem rfToolsManualDimensionItem;
    public static DevelopersDelightItem developersDelightItem;

    public static UnknownDimlet unknownDimlet;
    public static DimletTemplate dimletTemplate;
    public static KnownDimlet knownDimlet;
    public static EmptyDimensionTab emptyDimensionTab;
    public static RealizedDimensionTab realizedDimensionTab;
    public static DimensionMonitorItem dimensionMonitorItem;
    public static DimensionalShard dimensionalShard;

    public static TextModuleItem textModuleItem;
    public static EnergyModuleItem energyModuleItem;
    public static EnergyPlusModuleItem energyPlusModuleItem;
    public static DimensionModuleItem dimensionModuleItem;
    public static InventoryModuleItem inventoryModuleItem;
    public static ClockModuleItem clockModuleItem;
    public static FluidModuleItem fluidModuleItem;
    public static FluidPlusModuleItem fluidPlusModuleItem;

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

        rfToolsManualDimensionItem = new RFToolsManualDimensionItem();
        rfToolsManualDimensionItem.setUnlocalizedName("RFToolsManualDimension");
        rfToolsManualDimensionItem.setCreativeTab(RFTools.tabRfTools);
        rfToolsManualDimensionItem.setTextureName(RFTools.MODID + ":rftoolsManualDimension");
        GameRegistry.registerItem(rfToolsManualDimensionItem, "rfToolsManualDimensionItem");

        developersDelightItem = new DevelopersDelightItem();
        developersDelightItem.setUnlocalizedName("DevelopersDelight");
        developersDelightItem.setCreativeTab(RFTools.tabRfTools);
        developersDelightItem.setTextureName(RFTools.MODID + ":developersDelightItem");
        GameRegistry.registerItem(developersDelightItem, "developersDelightItem");

        initScreenModuleItems();
        initDimensionItems();
    }

    private static void initScreenModuleItems() {
        textModuleItem = new TextModuleItem();
        textModuleItem.setUnlocalizedName("TextModule");
        textModuleItem.setCreativeTab(RFTools.tabRfTools);
        textModuleItem.setTextureName(RFTools.MODID + ":modules/textModuleItem");
        GameRegistry.registerItem(textModuleItem, "textModuleItem");

        inventoryModuleItem = new InventoryModuleItem();
        inventoryModuleItem.setUnlocalizedName("InventoryModule");
        inventoryModuleItem.setCreativeTab(RFTools.tabRfTools);
        inventoryModuleItem.setTextureName(RFTools.MODID + ":modules/inventoryModuleItem");
        GameRegistry.registerItem(inventoryModuleItem, "inventoryModuleItem");

        energyModuleItem = new EnergyModuleItem();
        energyModuleItem.setUnlocalizedName("EnergyModule");
        energyModuleItem.setCreativeTab(RFTools.tabRfTools);
        energyModuleItem.setTextureName(RFTools.MODID + ":modules/energyModuleItem");
        GameRegistry.registerItem(energyModuleItem, "energyModuleItem");

        energyPlusModuleItem = new EnergyPlusModuleItem();
        energyPlusModuleItem.setUnlocalizedName("EnergyPlusModule");
        energyPlusModuleItem.setCreativeTab(RFTools.tabRfTools);
        energyPlusModuleItem.setTextureName(RFTools.MODID + ":modules/energyPlusModuleItem");
        GameRegistry.registerItem(energyPlusModuleItem, "energyPlusModuleItem");

        dimensionModuleItem = new DimensionModuleItem();
        dimensionModuleItem.setUnlocalizedName("DimensionModule");
        dimensionModuleItem.setCreativeTab(RFTools.tabRfTools);
        dimensionModuleItem.setTextureName(RFTools.MODID + ":modules/dimensionModuleItem");
        GameRegistry.registerItem(dimensionModuleItem, "dimensionModuleItem");

        clockModuleItem = new ClockModuleItem();
        clockModuleItem.setUnlocalizedName("ClockModule");
        clockModuleItem.setCreativeTab(RFTools.tabRfTools);
        clockModuleItem.setTextureName(RFTools.MODID + ":modules/clockModuleItem");
        GameRegistry.registerItem(clockModuleItem, "clockModuleItem");

        fluidModuleItem = new FluidModuleItem();
        fluidModuleItem.setUnlocalizedName("FluidModule");
        fluidModuleItem.setCreativeTab(RFTools.tabRfTools);
        fluidModuleItem.setTextureName(RFTools.MODID + ":modules/fluidModuleItem");
        GameRegistry.registerItem(fluidModuleItem, "fluidModuleItem");

        fluidPlusModuleItem = new FluidPlusModuleItem();
        fluidPlusModuleItem.setUnlocalizedName("FluidPlusModule");
        fluidPlusModuleItem.setCreativeTab(RFTools.tabRfTools);
        fluidPlusModuleItem.setTextureName(RFTools.MODID + ":modules/fluidPlusModuleItem");
        GameRegistry.registerItem(fluidPlusModuleItem, "fluidPlusModuleItem");
    }

    private static void initDimensionItems() {
        unknownDimlet = new UnknownDimlet();
        unknownDimlet.setUnlocalizedName("UnknownDimlet");
        unknownDimlet.setCreativeTab(RFTools.tabRfToolsDimlets);
        unknownDimlet.setTextureName(RFTools.MODID + ":unknownDimletItem");
        GameRegistry.registerItem(unknownDimlet, "unknownDimlet");

        knownDimlet = new KnownDimlet();
        knownDimlet.setUnlocalizedName("KnownDimlet");
        knownDimlet.setCreativeTab(RFTools.tabRfToolsDimlets);
        GameRegistry.registerItem(knownDimlet, "knownDimlet");

        dimletTemplate = new DimletTemplate();
        dimletTemplate.setUnlocalizedName("DimletTemplate");
        dimletTemplate.setCreativeTab(RFTools.tabRfToolsDimlets);
        dimletTemplate.setTextureName(RFTools.MODID + ":dimletTemplateItem");
        GameRegistry.registerItem(dimletTemplate, "dimletTemplate");

        emptyDimensionTab = new EmptyDimensionTab();
        emptyDimensionTab.setUnlocalizedName("EmptyDimensionTab");
        emptyDimensionTab.setCreativeTab(RFTools.tabRfTools);
        emptyDimensionTab.setTextureName(RFTools.MODID + ":emptyDimensionTabItem");
        GameRegistry.registerItem(emptyDimensionTab, "emptyDimensionTab");

        realizedDimensionTab = new RealizedDimensionTab();
        realizedDimensionTab.setUnlocalizedName("RealizedDimensionTab");
        realizedDimensionTab.setCreativeTab(RFTools.tabRfTools);
        realizedDimensionTab.setTextureName(RFTools.MODID + ":realizedDimensionTabItem");
        GameRegistry.registerItem(realizedDimensionTab, "realizedDimensionTab");

        dimensionMonitorItem = new DimensionMonitorItem();
        dimensionMonitorItem.setUnlocalizedName("DimensionMonitor");
        dimensionMonitorItem.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerItem(dimensionMonitorItem, "dimensionMonitorItem");

        dimensionalShard = new DimensionalShard();
        dimensionalShard.setUnlocalizedName("DimensionalShard");
        dimensionalShard.setCreativeTab(RFTools.tabRfTools);
        dimensionalShard.setTextureName(RFTools.MODID + ":dimensionalShardItem");
        GameRegistry.registerItem(dimensionalShard, "dimensionalShardItem");
    }
}
