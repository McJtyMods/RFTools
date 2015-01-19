package com.mcjty.rftools.blocks;

import com.mcjty.container.GenericItemBlock;
import com.mcjty.rftools.blocks.crafter.CrafterBlock;
import com.mcjty.rftools.blocks.crafter.CrafterBlockTileEntity1;
import com.mcjty.rftools.blocks.crafter.CrafterBlockTileEntity2;
import com.mcjty.rftools.blocks.crafter.CrafterBlockTileEntity3;
import com.mcjty.rftools.blocks.dimlets.*;
import com.mcjty.rftools.blocks.endergen.*;
import com.mcjty.rftools.blocks.infuser.MachineInfuserBlock;
import com.mcjty.rftools.blocks.infuser.MachineInfuserTileEntity;
import com.mcjty.rftools.blocks.itemfilter.ItemFilterBlock;
import com.mcjty.rftools.blocks.itemfilter.ItemFilterTileEntity;
import com.mcjty.rftools.blocks.logic.*;
import com.mcjty.rftools.blocks.monitor.RFMonitorBlock;
import com.mcjty.rftools.blocks.monitor.RFMonitorBlockTileEntity;
import com.mcjty.rftools.blocks.relay.RelayBlock;
import com.mcjty.rftools.blocks.relay.RelayTileEntity;
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
    public static TeleportBeamBlock teleportBeamBlock;

    public static EndergenicBlock endergenicBlock;
    public static PearlInjectorBlock pearlInjectorBlock;
    public static EnderMonitorBlock enderMonitorBlock;

    public static SequencerBlock sequencerBlock;
    public static TimerBlock timerBlock;

    public static ShieldBlock shieldBlock;
    public static InvisibleShieldBlock invisibleShieldBlock;
    public static ShieldBlockNOpaquePass1 shieldBlockNOpaquePass1;
    public static ShieldBlockNOpaquePass0 shieldBlockNOpaquePass0;
    public static ShieldBlockOpaquePass1 shieldBlockOpaquePass1;
    public static ShieldBlockOpaquePass0 shieldBlockOpaquePass0;
    public static ShieldBlockNOpaquePass1NN shieldBlockNOpaquePass1NN;
    public static ShieldBlockNOpaquePass0NN shieldBlockNOpaquePass0NN;
    public static ShieldBlockOpaquePass1NN shieldBlockOpaquePass1NN;
    public static ShieldBlockOpaquePass0NN shieldBlockOpaquePass0NN;
    public static SolidShieldBlock solidShieldBlock;
    public static ShieldTemplateBlock shieldTemplateBlock;

    public static MachineInfuserBlock machineInfuserBlock;

    public static DimletResearcherBlock dimletResearcherBlock;
    public static DimletScramblerBlock dimletScramblerBlock;
    public static DimensionEnscriberBlock dimensionEnscriberBlock;
    public static DimensionBuilderBlock dimensionBuilderBlock;
    public static DimensionBuilderBlock creativeDimensionBuilderBlock;
    public static DimensionEditorBlock dimensionEditorBlock;
    public static DimensionMonitorBlock dimensionMonitorBlock;

    public static DimensionalShardBlock dimensionalShardBlock;
    public static DimensionalBlankBlock dimensionalBlankBlock;
    public static DimensionalBlock dimensionalBlock;
    public static DimensionalSmallBlocks dimensionalSmallBlocks;
    public static DimensionalCrossBlock dimensionalCrossBlock;
    public static DimensionalCross2Block dimensionalCross2Block;
    public static DimensionalPattern1Block dimensionalPattern1Block;
    public static DimensionalPattern2Block dimensionalPattern2Block;
    public static ActivityProbeBlock activityProbeBlock;

    public static void init() {
        monitorBlock = new RFMonitorBlock();
        GameRegistry.registerBlock(monitorBlock, GenericItemBlock.class, "rfMonitorBlock");
        GameRegistry.registerTileEntity(RFMonitorBlockTileEntity.class, "RFMonitorTileEntity");

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

        initDimletBlocks();
        initTeleporterBlocks();
        initEndergenicBlocks();
        initLogicBlocks();
        initShieldBlocks();
        initBaseBlocks();
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
    }

    private static void initCrafterBlocks() {
        crafterBlock1 = new CrafterBlock("crafterBlock1", "machineCrafter1", CrafterBlockTileEntity1.class);
        GameRegistry.registerBlock(crafterBlock1, GenericItemBlock.class, "crafterBlock1");
        crafterBlock2 = new CrafterBlock("crafterBlock2", "machineCrafter2", CrafterBlockTileEntity2.class);
        GameRegistry.registerBlock(crafterBlock2, GenericItemBlock.class, "crafterBlock2");
        crafterBlock3 = new CrafterBlock("crafterBlock3", "machineCrafter3", CrafterBlockTileEntity3.class);
        GameRegistry.registerBlock(crafterBlock3, GenericItemBlock.class, "crafterBlock3");
        GameRegistry.registerTileEntity(CrafterBlockTileEntity1.class, "CrafterTileEntity1");
        GameRegistry.registerTileEntity(CrafterBlockTileEntity2.class, "CrafterTileEntity2");
        GameRegistry.registerTileEntity(CrafterBlockTileEntity3.class, "CrafterTileEntity3");
    }

    private static void initShieldBlocks() {
        shieldBlock = new ShieldBlock();
        GameRegistry.registerBlock(shieldBlock, GenericItemBlock.class, "shieldBlock");
        GameRegistry.registerTileEntity(ShieldTileEntity.class, "ShieldTileEntity");

        invisibleShieldBlock = new InvisibleShieldBlock();
        GameRegistry.registerBlock(invisibleShieldBlock, "invisibleShieldBlock");

        shieldBlockNOpaquePass1 = new ShieldBlockNOpaquePass1();
        GameRegistry.registerBlock(shieldBlockNOpaquePass1, "shieldBlockNOpaquePass1");
        shieldBlockNOpaquePass0 = new ShieldBlockNOpaquePass0();
        GameRegistry.registerBlock(shieldBlockNOpaquePass0, "shieldBlockNOpaquePass0");
        shieldBlockOpaquePass1 = new ShieldBlockOpaquePass1();
        GameRegistry.registerBlock(shieldBlockOpaquePass1, "shieldBlockOpaquePass1");
        shieldBlockOpaquePass0 = new ShieldBlockOpaquePass0();
        GameRegistry.registerBlock(shieldBlockOpaquePass0, "shieldBlockOpaquePass0");
        shieldBlockNOpaquePass1NN = new ShieldBlockNOpaquePass1NN();
        GameRegistry.registerBlock(shieldBlockNOpaquePass1NN, "shieldBlockNOpaquePass1NN");
        shieldBlockNOpaquePass0NN = new ShieldBlockNOpaquePass0NN();
        GameRegistry.registerBlock(shieldBlockNOpaquePass0NN, "shieldBlockNOpaquePass0NN");
        shieldBlockOpaquePass1NN = new ShieldBlockOpaquePass1NN();
        GameRegistry.registerBlock(shieldBlockOpaquePass1NN, "shieldBlockOpaquePass1NN");
        shieldBlockOpaquePass0NN = new ShieldBlockOpaquePass0NN();
        GameRegistry.registerBlock(shieldBlockOpaquePass0NN, "shieldBlockOpaquePass0NN");

        solidShieldBlock = new SolidShieldBlock();
        GameRegistry.registerBlock(solidShieldBlock, "solidShieldBlock");
        GameRegistry.registerTileEntity(ShieldBlockTileEntity.class, "ShieldBlockTileEntity");
        shieldTemplateBlock = new ShieldTemplateBlock();
        GameRegistry.registerBlock(shieldTemplateBlock, "shieldTemplateBlock");
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
        teleportBeamBlock = new TeleportBeamBlock();
        GameRegistry.registerBlock(teleportBeamBlock, "teleportBeamBlock");
    }
}
