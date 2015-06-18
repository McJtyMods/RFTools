package mcjty.rftools.items;

import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.dimletconstruction.DimletConstructionSetup;
import mcjty.rftools.blocks.dimlets.DimletSetup;
import mcjty.rftools.blocks.environmental.EnvironmentalSetup;
import mcjty.rftools.blocks.screens.ScreenSetup;
import mcjty.rftools.blocks.spaceprojector.SpaceProjectorSetup;
import mcjty.rftools.blocks.storage.ModularStorageSetup;
import mcjty.rftools.blocks.teleporter.TeleporterSetup;
import mcjty.rftools.items.devdelight.DevelopersDelightItem;
import mcjty.rftools.items.devdelight.ShardWandItem;
import mcjty.rftools.items.devdelight.OrphaningWandItem;
import mcjty.rftools.items.manual.RFToolsManualDimensionItem;
import mcjty.rftools.items.manual.RFToolsManualItem;
import mcjty.rftools.items.netmonitor.NetworkMonitorItem;
import mcjty.rftools.items.smartwrench.SmartWrenchItem;
import cpw.mods.fml.common.registry.GameRegistry;

public final class ModItems {
    public static NetworkMonitorItem networkMonitorItem;
    public static RFToolsManualItem rfToolsManualItem;
    public static RFToolsManualDimensionItem rfToolsManualDimensionItem;
    public static DevelopersDelightItem developersDelightItem;
    public static ShardWandItem shardWandItem;
    public static OrphaningWandItem orphaningWandItem;
    public static SmartWrenchItem smartWrenchItem;

    public static void init() {
        setupVariousItems();

        SpaceProjectorSetup.setupItems();
        TeleporterSetup.setupItems();
        EnvironmentalSetup.setupItems();
        ScreenSetup.setupItems();
        DimletSetup.setupItems();
        DimletConstructionSetup.setupItems();
        ModularStorageSetup.setupItems();
    }

    private static void setupVariousItems() {
        smartWrenchItem = new SmartWrenchItem();
        smartWrenchItem.setUnlocalizedName("SmartWrench");
        smartWrenchItem.setCreativeTab(RFTools.tabRfTools);
        smartWrenchItem.setTextureName(RFTools.MODID + ":smartWrenchItem");
        GameRegistry.registerItem(smartWrenchItem, "smartWrenchItem");

        networkMonitorItem = new NetworkMonitorItem();
        networkMonitorItem.setUnlocalizedName("NetworkMonitor");
        networkMonitorItem.setCreativeTab(RFTools.tabRfTools);
        networkMonitorItem.setTextureName(RFTools.MODID + ":networkMonitorItem");
        GameRegistry.registerItem(networkMonitorItem, "networkMonitorItem");

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

        shardWandItem = new ShardWandItem();
        shardWandItem.setUnlocalizedName("ShardWand");
        shardWandItem.setCreativeTab(RFTools.tabRfTools);
        shardWandItem.setTextureName(RFTools.MODID + ":shardWandItem");
        GameRegistry.registerItem(shardWandItem, "shardWandItem");

        orphaningWandItem = new OrphaningWandItem();
        orphaningWandItem.setUnlocalizedName("OrphaningWand");
        orphaningWandItem.setCreativeTab(RFTools.tabRfTools);
        orphaningWandItem.setTextureName(RFTools.MODID + ":orphaningWandItem");
        GameRegistry.registerItem(orphaningWandItem, "orphaningWandItem");
    }

}
