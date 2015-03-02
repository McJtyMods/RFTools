package com.mcjty.rftools.items;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.items.devdelight.DevelopersDelightItem;
import com.mcjty.rftools.items.dimensionmonitor.DimensionMonitorItem;
import com.mcjty.rftools.items.dimensionmonitor.PhasedFieldGeneratorItem;
import com.mcjty.rftools.items.dimlets.*;
import com.mcjty.rftools.items.envmodules.*;
import com.mcjty.rftools.items.manual.RFToolsManualDimensionItem;
import com.mcjty.rftools.items.manual.RFToolsManualItem;
import com.mcjty.rftools.items.netmonitor.NetworkMonitorItem;
import com.mcjty.rftools.items.parts.*;
import com.mcjty.rftools.items.screenmodules.*;
import com.mcjty.rftools.items.teleportprobe.ChargedPorterItem;
import com.mcjty.rftools.items.teleportprobe.TeleportProbeItem;
import cpw.mods.fml.common.registry.GameRegistry;

public final class ModItems {
    public static NetworkMonitorItem networkMonitorItem;
    public static TeleportProbeItem teleportProbeItem;
    public static ChargedPorterItem chargedPorterItem;
    public static RFToolsManualItem rfToolsManualItem;
    public static RFToolsManualDimensionItem rfToolsManualDimensionItem;
    public static DevelopersDelightItem developersDelightItem;

    public static UnknownDimlet unknownDimlet;
    public static DimletTemplate dimletTemplate;
    public static KnownDimlet knownDimlet;
    public static EmptyDimensionTab emptyDimensionTab;
    public static RealizedDimensionTab realizedDimensionTab;
    public static DimensionMonitorItem dimensionMonitorItem;
    public static PhasedFieldGeneratorItem phasedFieldGeneratorItem;
    public static DimensionalShard dimensionalShard;

    public static TextModuleItem textModuleItem;
    public static EnergyModuleItem energyModuleItem;
    public static EnergyPlusModuleItem energyPlusModuleItem;
    public static DimensionModuleItem dimensionModuleItem;
    public static InventoryModuleItem inventoryModuleItem;
    public static InventoryPlusModuleItem inventoryPlusModuleItem;
    public static ClockModuleItem clockModuleItem;
    public static FluidModuleItem fluidModuleItem;
    public static FluidPlusModuleItem fluidPlusModuleItem;
    public static CounterModuleItem counterModuleItem;
    public static CounterPlusModuleItem counterPlusModuleItem;
    public static RedstoneModuleItem redstoneModuleItem;

    public static RegenerationEModuleItem regenerationEModuleItem;
    public static RegenerationPlusEModuleItem regenerationPlusEModuleItem;
    public static SpeedEModuleItem speedEModuleItem;
    public static SpeedPlusEModuleItem speedPlusEModuleItem;
    public static HasteEModuleItem hasteEModuleItem;
    public static HastePlusEModuleItem hastePlusEModuleItem;
    public static SaturationEModuleItem saturationEModuleItem;
    public static SaturationPlusEModuleItem saturationPlusEModuleItem;
    public static FeatherFallingEModuleItem featherFallingEModuleItem;
    public static FeatherFallingPlusEModuleItem featherFallingPlusEModuleItem;
    public static FlightEModuleItem flightEModuleItem;

    public static DimletBaseItem dimletBaseItem;
    public static DimletControlCircuitItem dimletControlCircuitItem;
    public static DimletEnergyModuleItem dimletEnergyModuleItem;
    public static DimletMemoryUnitItem dimletMemoryUnitItem;
    public static DimletTypeControllerItem dimletTypeControllerItem;
    public static SyringeItem syringeItem;
    public static PeaceEssenceItem peaceEssenceItem;
    public static EfficiencyEssenceItem efficiencyEssenceItem;
    public static MediocreEfficiencyEssenceItem mediocreEfficiencyEssenceItem;

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

        chargedPorterItem = new ChargedPorterItem();
        chargedPorterItem.setUnlocalizedName("ChargedPorter");
        chargedPorterItem.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerItem(chargedPorterItem, "chargedPorterItem");

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

        initEnvironmentModuleItems();
        initScreenModuleItems();
        initDimensionItems();
        initDimletPartItems();
    }

    private static void initDimletPartItems() {
        dimletBaseItem = new DimletBaseItem();
        dimletBaseItem.setUnlocalizedName("DimletBase");
        dimletBaseItem.setCreativeTab(RFTools.tabRfTools);
        dimletBaseItem.setTextureName(RFTools.MODID + ":parts/dimletBase");
        GameRegistry.registerItem(dimletBaseItem, "dimletBaseItem");

        dimletControlCircuitItem = new DimletControlCircuitItem();
        dimletControlCircuitItem.setUnlocalizedName("DimletControlCircuit");
        dimletControlCircuitItem.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerItem(dimletControlCircuitItem, "dimletControlCircuitItem");

        dimletEnergyModuleItem = new DimletEnergyModuleItem();
        dimletEnergyModuleItem.setUnlocalizedName("DimletEnergyModule");
        dimletEnergyModuleItem.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerItem(dimletEnergyModuleItem, "dimletEnergyModuleItem");

        dimletMemoryUnitItem = new DimletMemoryUnitItem();
        dimletMemoryUnitItem.setUnlocalizedName("DimletMemoryUnit");
        dimletMemoryUnitItem.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerItem(dimletMemoryUnitItem, "dimletMemoryUnitItem");

        dimletTypeControllerItem = new DimletTypeControllerItem();
        dimletTypeControllerItem.setUnlocalizedName("DimletTypeController");
        dimletTypeControllerItem.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerItem(dimletTypeControllerItem, "dimletTypeControllerItem");

        syringeItem = new SyringeItem();
        syringeItem.setUnlocalizedName("SyringeItem");
        syringeItem.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerItem(syringeItem, "syringeItem");

        peaceEssenceItem = new PeaceEssenceItem();
        peaceEssenceItem.setUnlocalizedName("PeaceEssence");
        peaceEssenceItem.setCreativeTab(RFTools.tabRfTools);
        peaceEssenceItem.setTextureName(RFTools.MODID + ":parts/peaceEssence");
        GameRegistry.registerItem(peaceEssenceItem, "peaceEssenceItem");

        efficiencyEssenceItem = new EfficiencyEssenceItem();
        efficiencyEssenceItem.setUnlocalizedName("EfficiencyEssence");
        efficiencyEssenceItem.setCreativeTab(RFTools.tabRfTools);
        efficiencyEssenceItem.setTextureName(RFTools.MODID + ":parts/efficiencyEssence");
        GameRegistry.registerItem(efficiencyEssenceItem, "efficiencyEssenceItem");

        mediocreEfficiencyEssenceItem = new MediocreEfficiencyEssenceItem();
        mediocreEfficiencyEssenceItem.setUnlocalizedName("MediocreEfficiencyEssence");
        mediocreEfficiencyEssenceItem.setCreativeTab(RFTools.tabRfTools);
        mediocreEfficiencyEssenceItem.setTextureName(RFTools.MODID + ":parts/mediocreEfficiencyEssence");
        GameRegistry.registerItem(mediocreEfficiencyEssenceItem, "mediocreEfficiencyEssenceItem");
    }

    private static void initEnvironmentModuleItems() {
        regenerationEModuleItem = new RegenerationEModuleItem();
        regenerationEModuleItem.setUnlocalizedName("RegenerationEModule");
        regenerationEModuleItem.setCreativeTab(RFTools.tabRfTools);
        regenerationEModuleItem.setTextureName(RFTools.MODID + ":envmodules/regenerationEModuleItem");
        GameRegistry.registerItem(regenerationEModuleItem, "regenerationEModuleItem");

        regenerationPlusEModuleItem = new RegenerationPlusEModuleItem();
        regenerationPlusEModuleItem.setUnlocalizedName("RegenerationPlusEModule");
        regenerationPlusEModuleItem.setCreativeTab(RFTools.tabRfTools);
        regenerationPlusEModuleItem.setTextureName(RFTools.MODID + ":envmodules/regenerationPlusEModuleItem");
        GameRegistry.registerItem(regenerationPlusEModuleItem, "regenerationPlusEModuleItem");

        speedEModuleItem = new SpeedEModuleItem();
        speedEModuleItem.setUnlocalizedName("SpeedEModule");
        speedEModuleItem.setCreativeTab(RFTools.tabRfTools);
        speedEModuleItem.setTextureName(RFTools.MODID + ":envmodules/speedEModuleItem");
        GameRegistry.registerItem(speedEModuleItem, "speedEModuleItem");

        speedPlusEModuleItem = new SpeedPlusEModuleItem();
        speedPlusEModuleItem.setUnlocalizedName("SpeedPlusEModule");
        speedPlusEModuleItem.setCreativeTab(RFTools.tabRfTools);
        speedPlusEModuleItem.setTextureName(RFTools.MODID + ":envmodules/speedPlusEModuleItem");
        GameRegistry.registerItem(speedPlusEModuleItem, "speedPlusEModuleItem");

        hasteEModuleItem = new HasteEModuleItem();
        hasteEModuleItem.setUnlocalizedName("HasteEModule");
        hasteEModuleItem.setCreativeTab(RFTools.tabRfTools);
        hasteEModuleItem.setTextureName(RFTools.MODID + ":envmodules/hasteEModuleItem");
        GameRegistry.registerItem(hasteEModuleItem, "hasteEModuleItem");

        hastePlusEModuleItem = new HastePlusEModuleItem();
        hastePlusEModuleItem.setUnlocalizedName("HastePlusEModule");
        hastePlusEModuleItem.setCreativeTab(RFTools.tabRfTools);
        hastePlusEModuleItem.setTextureName(RFTools.MODID + ":envmodules/hastePlusEModuleItem");
        GameRegistry.registerItem(hastePlusEModuleItem, "hastePlusEModuleItem");

        saturationEModuleItem = new SaturationEModuleItem();
        saturationEModuleItem.setUnlocalizedName("SaturationEModule");
        saturationEModuleItem.setCreativeTab(RFTools.tabRfTools);
        saturationEModuleItem.setTextureName(RFTools.MODID + ":envmodules/saturationEModuleItem");
        GameRegistry.registerItem(saturationEModuleItem, "saturationEModuleItem");

        saturationPlusEModuleItem = new SaturationPlusEModuleItem();
        saturationPlusEModuleItem.setUnlocalizedName("SaturationPlusEModule");
        saturationPlusEModuleItem.setCreativeTab(RFTools.tabRfTools);
        saturationPlusEModuleItem.setTextureName(RFTools.MODID + ":envmodules/saturationPlusEModuleItem");
        GameRegistry.registerItem(saturationPlusEModuleItem, "saturationPlusEModuleItem");

        featherFallingEModuleItem = new FeatherFallingEModuleItem();
        featherFallingEModuleItem.setUnlocalizedName("FeatherFallingEModule");
        featherFallingEModuleItem.setCreativeTab(RFTools.tabRfTools);
        featherFallingEModuleItem.setTextureName(RFTools.MODID + ":envmodules/featherfallingEModuleItem");
        GameRegistry.registerItem(featherFallingEModuleItem, "featherFallingEModuleItem");

        featherFallingPlusEModuleItem = new FeatherFallingPlusEModuleItem();
        featherFallingPlusEModuleItem.setUnlocalizedName("FeatherFallingPlusEModule");
        featherFallingPlusEModuleItem.setCreativeTab(RFTools.tabRfTools);
        featherFallingPlusEModuleItem.setTextureName(RFTools.MODID + ":envmodules/featherFallingPlusEModuleItem");
        GameRegistry.registerItem(featherFallingPlusEModuleItem, "featherFallingPlusEModuleItem");

        flightEModuleItem = new FlightEModuleItem();
        flightEModuleItem.setUnlocalizedName("FlightEModule");
        flightEModuleItem.setCreativeTab(RFTools.tabRfTools);
        flightEModuleItem.setTextureName(RFTools.MODID + ":envmodules/flightEModuleItem");
        GameRegistry.registerItem(flightEModuleItem, "flightEModuleItem");
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

        inventoryPlusModuleItem = new InventoryPlusModuleItem();
        inventoryPlusModuleItem.setUnlocalizedName("InventoryPlusModule");
        inventoryPlusModuleItem.setCreativeTab(RFTools.tabRfTools);
        inventoryPlusModuleItem.setTextureName(RFTools.MODID + ":modules/inventoryPlusModuleItem");
        GameRegistry.registerItem(inventoryPlusModuleItem, "inventoryPlusModuleItem");

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

        counterModuleItem = new CounterModuleItem();
        counterModuleItem.setUnlocalizedName("CounterModule");
        counterModuleItem.setCreativeTab(RFTools.tabRfTools);
        counterModuleItem.setTextureName(RFTools.MODID + ":modules/counterModuleItem");
        GameRegistry.registerItem(counterModuleItem, "counterModuleItem");

        counterPlusModuleItem = new CounterPlusModuleItem();
        counterPlusModuleItem.setUnlocalizedName("CounterPlusModule");
        counterPlusModuleItem.setCreativeTab(RFTools.tabRfTools);
        counterPlusModuleItem.setTextureName(RFTools.MODID + ":modules/counterPlusModuleItem");
        GameRegistry.registerItem(counterPlusModuleItem, "counterPlusModuleItem");

        redstoneModuleItem = new RedstoneModuleItem();
        redstoneModuleItem.setUnlocalizedName("RedstoneModule");
        redstoneModuleItem.setCreativeTab(RFTools.tabRfTools);
        redstoneModuleItem.setTextureName(RFTools.MODID + ":modules/redstoneModuleItem");
        GameRegistry.registerItem(redstoneModuleItem, "redstoneModuleItem");
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

        phasedFieldGeneratorItem = new PhasedFieldGeneratorItem();
        phasedFieldGeneratorItem.setUnlocalizedName("PhasedFieldGenerator");
        phasedFieldGeneratorItem.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerItem(phasedFieldGeneratorItem, "phasedFieldGeneratorItem");

        dimensionalShard = new DimensionalShard();
        dimensionalShard.setUnlocalizedName("DimensionalShard");
        dimensionalShard.setCreativeTab(RFTools.tabRfTools);
        dimensionalShard.setTextureName(RFTools.MODID + ":dimensionalShardItem");
        GameRegistry.registerItem(dimensionalShard, "dimensionalShardItem");
    }
}
