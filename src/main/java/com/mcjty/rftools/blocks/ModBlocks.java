package com.mcjty.rftools.blocks;

import com.mcjty.container.GenericItemBlock;
import com.mcjty.rftools.blocks.crafter.CrafterBlock;
import com.mcjty.rftools.blocks.crafter.CrafterBlockTileEntity1;
import com.mcjty.rftools.blocks.crafter.CrafterBlockTileEntity2;
import com.mcjty.rftools.blocks.crafter.CrafterBlockTileEntity3;
import com.mcjty.rftools.blocks.dimletconstruction.*;
import com.mcjty.rftools.blocks.dimlets.*;
import com.mcjty.rftools.blocks.endergen.*;
import com.mcjty.rftools.blocks.environmental.EnvironmentalControllerBlock;
import com.mcjty.rftools.blocks.environmental.EnvironmentalControllerTileEntity;
import com.mcjty.rftools.blocks.infuser.MachineInfuserBlock;
import com.mcjty.rftools.blocks.infuser.MachineInfuserTileEntity;
import com.mcjty.rftools.blocks.itemfilter.ItemFilterBlock;
import com.mcjty.rftools.blocks.itemfilter.ItemFilterTileEntity;
import com.mcjty.rftools.blocks.logic.*;
import com.mcjty.rftools.blocks.monitor.LiquidMonitorBlock;
import com.mcjty.rftools.blocks.monitor.LiquidMonitorBlockTileEntity;
import com.mcjty.rftools.blocks.monitor.RFMonitorBlock;
import com.mcjty.rftools.blocks.monitor.RFMonitorBlockTileEntity;
import com.mcjty.rftools.blocks.relay.RelayBlock;
import com.mcjty.rftools.blocks.relay.RelayTileEntity;
import com.mcjty.rftools.blocks.screens.ScreenBlock;
import com.mcjty.rftools.blocks.screens.ScreenControllerBlock;
import com.mcjty.rftools.blocks.screens.ScreenControllerTileEntity;
import com.mcjty.rftools.blocks.screens.ScreenTileEntity;
import com.mcjty.rftools.blocks.shards.*;
import com.mcjty.rftools.blocks.shield.*;
import com.mcjty.rftools.blocks.storagemonitor.StorageScannerBlock;
import com.mcjty.rftools.blocks.storagemonitor.StorageScannerTileEntity;
import com.mcjty.rftools.blocks.teleporter.*;
import cpw.mods.fml.common.registry.GameRegistry;

public final class ModBlocks {
    public static MachineFrame machineFrame;
    public static MachineBase machineBase;

    public static RFMonitorBlock monitorBlock;
    public static LiquidMonitorBlock liquidMonitorBlock;

    public static CrafterBlock crafterBlock1;
    public static CrafterBlock crafterBlock2;
    public static CrafterBlock crafterBlock3;

    public static StorageScannerBlock storageScannerBlock;

    public static RelayBlock relayBlock;

    public static ItemFilterBlock itemFilterBlock;

    public static MatterTransmitterBlock matterTransmitterBlock;
    public static MatterReceiverBlock matterReceiverBlock;
    public static DialingDeviceBlock dialingDeviceBlock;
    public static DestinationAnalyzerBlock destinationAnalyzerBlock;
    public static MatterBoosterBlock matterBoosterBlock;

    public static EndergenicBlock endergenicBlock;
    public static PearlInjectorBlock pearlInjectorBlock;
    public static EnderMonitorBlock enderMonitorBlock;

    public static SequencerBlock sequencerBlock;
    public static TimerBlock timerBlock;
    public static CounterBlock counterBlock;
    public static RedstoneTransmitterBlock redstoneTransmitterBlock;
    public static RedstoneReceiverBlock redstoneReceiverBlock;

    public static ShieldBlock shieldBlock;
    public static ShieldBlock shieldBlock2;
    public static InvisibleShieldBlock invisibleShieldBlock;
    public static SolidShieldBlock solidShieldBlock;
    public static ShieldTemplateBlock shieldTemplateBlock;

    public static MachineInfuserBlock machineInfuserBlock;

    public static DimletResearcherBlock dimletResearcherBlock;
    public static DimletScramblerBlock dimletScramblerBlock;
    public static DimletWorkbenchBlock dimletWorkbenchBlock;
    public static DimensionEnscriberBlock dimensionEnscriberBlock;
    public static DimensionBuilderBlock dimensionBuilderBlock;
    public static DimensionBuilderBlock creativeDimensionBuilderBlock;
    public static DimensionEditorBlock dimensionEditorBlock;
    public static DimensionMonitorBlock dimensionMonitorBlock;

    public static BiomeAbsorberBlock biomeAbsorberBlock;
    public static MaterialAbsorberBlock materialAbsorberBlock;
    public static LiquidAbsorberBlock liquidAbsorberBlock;
    public static TimeAbsorberBlock timeAbsorberBlock;

    public static DimensionalShardBlock dimensionalShardBlock;
    public static DimensionalBlankBlock dimensionalBlankBlock;
    public static DimensionalBlock dimensionalBlock;
    public static DimensionalSmallBlocks dimensionalSmallBlocks;
    public static DimensionalCrossBlock dimensionalCrossBlock;
    public static DimensionalCross2Block dimensionalCross2Block;
    public static DimensionalPattern1Block dimensionalPattern1Block;
    public static DimensionalPattern2Block dimensionalPattern2Block;
    public static ActivityProbeBlock activityProbeBlock;

    public static ScreenBlock screenBlock;
    public static ScreenControllerBlock screenControllerBlock;

    public static EnvironmentalControllerBlock environmentalControllerBlock;

    public static void init() {
        monitorBlock = new RFMonitorBlock();
        GameRegistry.registerBlock(monitorBlock, GenericItemBlock.class, "rfMonitorBlock");
        GameRegistry.registerTileEntity(RFMonitorBlockTileEntity.class, "RFMonitorTileEntity");

        liquidMonitorBlock = new LiquidMonitorBlock();
        GameRegistry.registerBlock(liquidMonitorBlock, GenericItemBlock.class, "liquidMonitorBlock");
        GameRegistry.registerTileEntity(LiquidMonitorBlockTileEntity.class, "LiquidMonitorBlockTileEntity");

        initCrafterBlocks();

        relayBlock = new RelayBlock();
        GameRegistry.registerBlock(relayBlock, GenericItemBlock.class, "relayBlock");
        GameRegistry.registerTileEntity(RelayTileEntity.class, "RelayTileEntity");

        itemFilterBlock = new ItemFilterBlock();
        GameRegistry.registerBlock(itemFilterBlock, GenericItemBlock.class, "itemFilterBlock");
        GameRegistry.registerTileEntity(ItemFilterTileEntity.class, "ItemFilterTileEntity");

        storageScannerBlock = new StorageScannerBlock();
        GameRegistry.registerBlock(storageScannerBlock, GenericItemBlock.class, "storageScannerBlock");
        GameRegistry.registerTileEntity(StorageScannerTileEntity.class, "StorageScannerTileEntity");

        machineInfuserBlock = new MachineInfuserBlock();
        GameRegistry.registerBlock(machineInfuserBlock, GenericItemBlock.class, "machineInfuserBlock");
        GameRegistry.registerTileEntity(MachineInfuserTileEntity.class, "MachineInfuserTileEntity");

        environmentalControllerBlock = new EnvironmentalControllerBlock();
        GameRegistry.registerBlock(environmentalControllerBlock, GenericItemBlock.class, "environmentalControllerBlock");
        GameRegistry.registerTileEntity(EnvironmentalControllerTileEntity.class, "EnvironmentalControllerTileEntity");

        initScreenBlocks();
        initDimletBlocks();
        initTeleporterBlocks();
        initEndergenicBlocks();
        initLogicBlocks();
        initShieldBlocks();
        initBaseBlocks();
    }

    private static void initScreenBlocks() {
        screenBlock = new ScreenBlock("screenBlock", ScreenTileEntity.class);
        GameRegistry.registerBlock(screenBlock, GenericItemBlock.class, "screenBlock");
        GameRegistry.registerTileEntity(ScreenTileEntity.class, "ScreenTileEntity");

        screenControllerBlock = new ScreenControllerBlock();
        GameRegistry.registerBlock(screenControllerBlock, GenericItemBlock.class, "screenControllerBlock");
        GameRegistry.registerTileEntity(ScreenControllerTileEntity.class, "ScreenControllerTileEntity");
    }

    private static void initBaseBlocks() {
        machineFrame = new MachineFrame();
        GameRegistry.registerBlock(machineFrame, "machineFrame");

        machineBase = new MachineBase();
        GameRegistry.registerBlock(machineBase, "machineBase");
    }

    private static void initDimletBlocks() {
        dimletResearcherBlock = new DimletResearcherBlock();
        GameRegistry.registerBlock(dimletResearcherBlock, GenericItemBlock.class, "dimletResearcherBlock");
        GameRegistry.registerTileEntity(DimletResearcherTileEntity.class, "DimletResearcherTileEntity");

        dimletScramblerBlock = new DimletScramblerBlock();
        GameRegistry.registerBlock(dimletScramblerBlock, GenericItemBlock.class, "dimletScramblerBlock");
        GameRegistry.registerTileEntity(DimletScramblerTileEntity.class, "DimletScramblerTileEntity");

        dimletWorkbenchBlock = new DimletWorkbenchBlock();
        GameRegistry.registerBlock(dimletWorkbenchBlock, GenericItemBlock.class, "dimletWorkbenchBlock");
        GameRegistry.registerTileEntity(DimletWorkbenchTileEntity.class, "DimletWorkbenchTileEntity");

        dimensionEnscriberBlock = new DimensionEnscriberBlock();
        GameRegistry.registerBlock(dimensionEnscriberBlock, GenericItemBlock.class, "dimensionEnscriberBlock");
        GameRegistry.registerTileEntity(DimensionEnscriberTileEntity.class, "DimensionEnscriberTileEntity");

        dimensionBuilderBlock = new DimensionBuilderBlock(false, "dimensionBuilderBlock");
        GameRegistry.registerBlock(dimensionBuilderBlock, GenericItemBlock.class, "dimensionBuilderBlock");
        GameRegistry.registerTileEntity(DimensionBuilderTileEntity.class, "DimensionBuilderTileEntity");

        creativeDimensionBuilderBlock = new DimensionBuilderBlock(true, "creativeDimensionBuilderBlock");
        GameRegistry.registerBlock(creativeDimensionBuilderBlock, GenericItemBlock.class, "creativeDimensionBuilderBlock");

        dimensionEditorBlock = new DimensionEditorBlock();
        GameRegistry.registerBlock(dimensionEditorBlock, GenericItemBlock.class, "dimensionEditorBlock");
        GameRegistry.registerTileEntity(DimensionEditorTileEntity.class, "DimensionEditorTileEntity");

        dimensionMonitorBlock = new DimensionMonitorBlock();
        GameRegistry.registerBlock(dimensionMonitorBlock, GenericItemBlock.class, "dimensionMonitorBlock");
        GameRegistry.registerTileEntity(DimensionMonitorTileEntity.class, "DimensionMonitorTileEntity");

        dimensionalShardBlock = new DimensionalShardBlock();
        GameRegistry.registerBlock(dimensionalShardBlock, "dimensionalShardBlock");
        dimensionalBlankBlock = new DimensionalBlankBlock();
        GameRegistry.registerBlock(dimensionalBlankBlock, "dimensionalBlankBlock");
        dimensionalBlock = new DimensionalBlock();
        GameRegistry.registerBlock(dimensionalBlock, "dimensionalBlock");
        dimensionalSmallBlocks = new DimensionalSmallBlocks();
        GameRegistry.registerBlock(dimensionalSmallBlocks, "dimensionalSmallBlocks");
        dimensionalCrossBlock = new DimensionalCrossBlock();
        GameRegistry.registerBlock(dimensionalCrossBlock, "dimensionalCrossBlock");
        dimensionalCross2Block = new DimensionalCross2Block();
        GameRegistry.registerBlock(dimensionalCross2Block, "dimensionalCross2Block");
        dimensionalPattern1Block = new DimensionalPattern1Block();
        GameRegistry.registerBlock(dimensionalPattern1Block, "dimensionalPattern1Block");
        dimensionalPattern2Block = new DimensionalPattern2Block();
        GameRegistry.registerBlock(dimensionalPattern2Block, "dimensionalPattern2Block");

        activityProbeBlock = new ActivityProbeBlock();
        GameRegistry.registerBlock(activityProbeBlock, "activityProbeBlock");

        biomeAbsorberBlock = new BiomeAbsorberBlock();
        GameRegistry.registerBlock(biomeAbsorberBlock, GenericItemBlock.class, "biomeAbsorberBlock");
        GameRegistry.registerTileEntity(BiomeAbsorberTileEntity.class, "BiomeAbsorberTileEntity");

        materialAbsorberBlock = new MaterialAbsorberBlock();
        GameRegistry.registerBlock(materialAbsorberBlock, GenericItemBlock.class, "materialAbsorberBlock");
        GameRegistry.registerTileEntity(MaterialAbsorberTileEntity.class, "MaterialAbsorberTileEntity");

        liquidAbsorberBlock = new LiquidAbsorberBlock();
        GameRegistry.registerBlock(liquidAbsorberBlock, GenericItemBlock.class, "liquidAbsorberBlock");
        GameRegistry.registerTileEntity(LiquidAbsorberTileEntity.class, "LiquidAbsorberTileEntity");

        timeAbsorberBlock = new TimeAbsorberBlock();
        GameRegistry.registerBlock(timeAbsorberBlock, GenericItemBlock.class, "timeAbsorberBlock");
        GameRegistry.registerTileEntity(TimeAbsorberTileEntity.class, "TimeAbsorberTileEntity");
    }

    private static void initCrafterBlocks() {
        crafterBlock1 = new CrafterBlock("crafterBlock1", "machineCrafter1", CrafterBlockTileEntity1.class);
        GameRegistry.registerBlock(crafterBlock1, GenericItemBlock.class, "crafterBlock1");
        GameRegistry.registerTileEntity(CrafterBlockTileEntity1.class, "CrafterTileEntity1");

        crafterBlock2 = new CrafterBlock("crafterBlock2", "machineCrafter2", CrafterBlockTileEntity2.class);
        GameRegistry.registerBlock(crafterBlock2, GenericItemBlock.class, "crafterBlock2");
        GameRegistry.registerTileEntity(CrafterBlockTileEntity2.class, "CrafterTileEntity2");

        crafterBlock3 = new CrafterBlock("crafterBlock3", "machineCrafter3", CrafterBlockTileEntity3.class);
        GameRegistry.registerBlock(crafterBlock3, GenericItemBlock.class, "crafterBlock3");
        GameRegistry.registerTileEntity(CrafterBlockTileEntity3.class, "CrafterTileEntity3");
    }

    private static void initShieldBlocks() {
        shieldBlock = new ShieldBlock("shieldBlock", ShieldTileEntity.class);
        GameRegistry.registerBlock(shieldBlock, GenericItemBlock.class, "shieldBlock");
        GameRegistry.registerTileEntity(ShieldTileEntity.class, "ShieldTileEntity");

        shieldBlock2 = new ShieldBlock("shieldBlock2", ShieldTileEntity2.class);
        GameRegistry.registerBlock(shieldBlock2, GenericItemBlock.class, "shieldBlock2");
        GameRegistry.registerTileEntity(ShieldTileEntity2.class, "ShieldTileEntity2");

        invisibleShieldBlock = new InvisibleShieldBlock();
        GameRegistry.registerBlock(invisibleShieldBlock, "invisibleShieldBlock");

        if (!ShieldConfiguration.disableShieldBlocksToUncorruptWorld) {
            solidShieldBlock = new SolidShieldBlock();
            GameRegistry.registerBlock(solidShieldBlock, "solidShieldBlock");
            GameRegistry.registerTileEntity(ShieldBlockTileEntity.class, "ShieldBlockTileEntity");
            shieldTemplateBlock = new ShieldTemplateBlock();
            GameRegistry.registerBlock(shieldTemplateBlock, "shieldTemplateBlock");
        }
    }

    private static void initLogicBlocks() {
        enderMonitorBlock = new EnderMonitorBlock();
        GameRegistry.registerBlock(enderMonitorBlock, GenericItemBlock.class, "enderMonitorBlock");
        GameRegistry.registerTileEntity(EnderMonitorTileEntity.class, "EnderMonitorTileEntity");

        sequencerBlock = new SequencerBlock();
        GameRegistry.registerBlock(sequencerBlock, GenericItemBlock.class, "sequencerBlock");
        GameRegistry.registerTileEntity(SequencerTileEntity.class, "SequencerTileEntity");

        timerBlock = new TimerBlock();
        GameRegistry.registerBlock(timerBlock, GenericItemBlock.class, "timerBlock");
        GameRegistry.registerTileEntity(TimerTileEntity.class, "TimerTileEntity");

        counterBlock = new CounterBlock();
        GameRegistry.registerBlock(counterBlock, GenericItemBlock.class, "counterBlock");
        GameRegistry.registerTileEntity(CounterTileEntity.class, "CounterTileEntity");

        redstoneTransmitterBlock = new RedstoneTransmitterBlock();
        GameRegistry.registerBlock(redstoneTransmitterBlock, GenericItemBlock.class, "redstoneTransmitterBlock");
        GameRegistry.registerTileEntity(RedstoneTransmitterTileEntity.class, "RedstoneTransmitterTileEntity");

        redstoneReceiverBlock = new RedstoneReceiverBlock();
        GameRegistry.registerBlock(redstoneReceiverBlock, RedstoneReceiverItemBlock.class, "redstoneReceiverBlock");
        GameRegistry.registerTileEntity(RedstoneReceiverTileEntity.class, "RedstoneReceiverTileEntity");
    }

    private static void initEndergenicBlocks() {
        endergenicBlock = new EndergenicBlock();
        GameRegistry.registerBlock(endergenicBlock, GenericItemBlock.class, "endergenicBlock");
        GameRegistry.registerTileEntity(EndergenicTileEntity.class, "EndergenicTileEntity");

        pearlInjectorBlock = new PearlInjectorBlock();
        GameRegistry.registerBlock(pearlInjectorBlock, GenericItemBlock.class, "pearlInjectorBlock");
        GameRegistry.registerTileEntity(PearlInjectorTileEntity.class, "PearlInjectorTileEntity");
    }

    private static void initTeleporterBlocks() {
        matterTransmitterBlock = new MatterTransmitterBlock();
        GameRegistry.registerBlock(matterTransmitterBlock, GenericItemBlock.class, "matterTransmitterBlock");
        GameRegistry.registerTileEntity(MatterTransmitterTileEntity.class, "MatterTransmitterTileEntity");

        matterReceiverBlock = new MatterReceiverBlock();
        GameRegistry.registerBlock(matterReceiverBlock, GenericItemBlock.class, "matterReceiverBlock");
        GameRegistry.registerTileEntity(MatterReceiverTileEntity.class, "MatterReceiverTileEntity");

        dialingDeviceBlock = new DialingDeviceBlock();
        GameRegistry.registerBlock(dialingDeviceBlock, GenericItemBlock.class, "dialingDeviceBlock");
        GameRegistry.registerTileEntity(DialingDeviceTileEntity.class, "DialingDeviceTileEntity");

        destinationAnalyzerBlock = new DestinationAnalyzerBlock();
        GameRegistry.registerBlock(destinationAnalyzerBlock, "destinationAnalyzerBlock");

        matterBoosterBlock = new MatterBoosterBlock();
        GameRegistry.registerBlock(matterBoosterBlock, "matterBoosterBlock");
    }
}
