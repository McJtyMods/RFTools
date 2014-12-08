package com.mcjty.rftools.blocks;

import com.mcjty.rftools.blocks.crafter.*;
import com.mcjty.rftools.blocks.dimlets.*;
import com.mcjty.rftools.blocks.endergen.*;
import com.mcjty.rftools.blocks.infuser.MachineInfuserBlock;
import com.mcjty.rftools.blocks.infuser.MachineInfuserItemBlock;
import com.mcjty.rftools.blocks.infuser.MachineInfuserTileEntity;
import com.mcjty.rftools.blocks.logic.*;
import com.mcjty.rftools.blocks.monitor.RFMonitorBlock;
import com.mcjty.rftools.blocks.monitor.RFMonitorBlockTileEntity;
import com.mcjty.rftools.blocks.monitor.RFMonitorItemBlock;
import com.mcjty.rftools.blocks.relay.RelayBlock;
import com.mcjty.rftools.blocks.relay.RelayItemBlock;
import com.mcjty.rftools.blocks.relay.RelayTileEntity;
import com.mcjty.rftools.blocks.shards.DimensionalShardBlock;
import com.mcjty.rftools.blocks.shield.*;
import com.mcjty.rftools.blocks.storagemonitor.StorageScannerBlock;
import com.mcjty.rftools.blocks.storagemonitor.StorageScannerItemBlock;
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
    public static DimensionalShardBlock dimensionalShardBlock;

    public static void init() {
        monitorBlock = new RFMonitorBlock();
        GameRegistry.registerBlock(monitorBlock, RFMonitorItemBlock.class, "rfMonitorBlock");
        GameRegistry.registerTileEntity(RFMonitorBlockTileEntity.class, "RFMonitorTileEntity");

        initCrafterBlocks();

        relayBlock = new RelayBlock();
        GameRegistry.registerBlock(relayBlock, RelayItemBlock.class, "relayBlock");
        GameRegistry.registerTileEntity(RelayTileEntity.class, "RelayTileEntity");

        storageScannerBlock = new StorageScannerBlock();
        GameRegistry.registerBlock(storageScannerBlock, StorageScannerItemBlock.class, "storageScannerBlock");
        GameRegistry.registerTileEntity(StorageScannerTileEntity.class, "StorageScannerTileEntity");

        machineInfuserBlock = new MachineInfuserBlock();
        GameRegistry.registerBlock(machineInfuserBlock, MachineInfuserItemBlock.class, "machineInfuserBlock");
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
        GameRegistry.registerBlock(dimletResearcherBlock, DimletResearcherItemBlock.class, "dimletResearcherBlock");
        GameRegistry.registerTileEntity(DimletResearcherTileEntity.class, "DimletResearcherTileEntity");

        dimletScramblerBlock = new DimletScramblerBlock();
        GameRegistry.registerBlock(dimletScramblerBlock, DimletScramblerItemBlock.class, "dimletScramblerBlock");
        GameRegistry.registerTileEntity(DimletScramblerTileEntity.class, "DimletScramblerTileEntity");

        dimensionEnscriberBlock = new DimensionEnscriberBlock();
        GameRegistry.registerBlock(dimensionEnscriberBlock, DimensionEnscriberItemBlock.class, "dimensionEnscriberBlock");
        GameRegistry.registerTileEntity(DimensionEnscriberTileEntity.class, "DimensionEnscriberTileEntity");

        dimensionBuilderBlock = new DimensionBuilderBlock();
        GameRegistry.registerBlock(dimensionBuilderBlock, DimensionBuilderItemBlock.class, "dimensionBuilderBlock");
        GameRegistry.registerTileEntity(DimensionBuilderTileEntity.class, "DimensionBuilderTileEntity");

        dimensionalShardBlock = new DimensionalShardBlock();
        GameRegistry.registerBlock(dimensionalShardBlock, "dimensionalShardBlock");
    }

    private static void initCrafterBlocks() {
        crafterBlock1 = new CrafterBlock("crafterBlock1", "machineCrafter1", CrafterBlockTileEntity1.class);
        GameRegistry.registerBlock(crafterBlock1, CrafterItemBlock.class, "crafterBlock1");
        crafterBlock2 = new CrafterBlock("crafterBlock2", "machineCrafter2", CrafterBlockTileEntity2.class);
        GameRegistry.registerBlock(crafterBlock2, CrafterItemBlock.class, "crafterBlock2");
        crafterBlock3 = new CrafterBlock("crafterBlock3", "machineCrafter3", CrafterBlockTileEntity3.class);
        GameRegistry.registerBlock(crafterBlock3, CrafterItemBlock.class, "crafterBlock3");
        GameRegistry.registerTileEntity(CrafterBlockTileEntity1.class, "CrafterTileEntity1");
        GameRegistry.registerTileEntity(CrafterBlockTileEntity2.class, "CrafterTileEntity2");
        GameRegistry.registerTileEntity(CrafterBlockTileEntity3.class, "CrafterTileEntity3");
    }

    private static void initShieldBlocks() {
        shieldBlock = new ShieldBlock();
        GameRegistry.registerBlock(shieldBlock, ShieldItemBlock.class, "shieldBlock");
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
        GameRegistry.registerBlock(enderMonitorBlock, EnderMonitorItemBlock.class, "enderMonitorBlock");
        GameRegistry.registerTileEntity(EnderMonitorTileEntity.class, "EnderMonitorTileEntity");

        sequencerBlock = new SequencerBlock();
        GameRegistry.registerBlock(sequencerBlock, SequencerItemBlock.class, "sequencerBlock");
        GameRegistry.registerTileEntity(SequencerTileEntity.class, "SequencerTileEntity");

        timerBlock = new TimerBlock();
        GameRegistry.registerBlock(timerBlock, TimerItemBlock.class, "timerBlock");
        GameRegistry.registerTileEntity(TimerTileEntity.class, "TimerTileEntity");
    }

    private static void initEndergenicBlocks() {
        endergenicBlock = new EndergenicBlock();
        GameRegistry.registerBlock(endergenicBlock, EndergenicItemBlock.class, "endergenicBlock");
        GameRegistry.registerTileEntity(EndergenicTileEntity.class, "EndergenicTileEntity");

        pearlInjectorBlock = new PearlInjectorBlock();
        GameRegistry.registerBlock(pearlInjectorBlock, PearlInjectorItemBlock.class, "pearlInjectorBlock");
        GameRegistry.registerTileEntity(PearlInjectorTileEntity.class, "PearlInjectorTileEntity");
    }

    private static void initTeleporterBlocks() {
        matterTransmitterBlock = new MatterTransmitterBlock();
        GameRegistry.registerBlock(matterTransmitterBlock, MatterTransmitterItemBlock.class, "matterTransmitterBlock");
        GameRegistry.registerTileEntity(MatterTransmitterTileEntity.class, "MatterTransmitterTileEntity");

        matterReceiverBlock = new MatterReceiverBlock();
        GameRegistry.registerBlock(matterReceiverBlock, MatterReceiverItemBlock.class, "matterReceiverBlock");
        GameRegistry.registerTileEntity(MatterReceiverTileEntity.class, "MatterReceiverTileEntity");

        dialingDeviceBlock = new DialingDeviceBlock();
        GameRegistry.registerBlock(dialingDeviceBlock, DialingDeviceItemBlock.class, "dialingDeviceBlock");
        GameRegistry.registerTileEntity(DialingDeviceTileEntity.class, "DialingDeviceTileEntity");

        destinationAnalyzerBlock = new DestinationAnalyzerBlock();
        GameRegistry.registerBlock(destinationAnalyzerBlock, "destinationAnalyzerBlock");
        teleportBeamBlock = new TeleportBeamBlock();
        GameRegistry.registerBlock(teleportBeamBlock, "teleportBeamBlock");
    }
}
