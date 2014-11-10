package com.mcjty.rftools.blocks;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.crafter.*;
import com.mcjty.rftools.blocks.endergen.*;
import com.mcjty.rftools.blocks.logic.*;
import com.mcjty.rftools.blocks.monitor.RFMonitorBlock;
import com.mcjty.rftools.blocks.monitor.RFMonitorBlockTileEntity;
import com.mcjty.rftools.blocks.monitor.RFMonitorItemBlock;
import com.mcjty.rftools.blocks.relay.RelayBlock;
import com.mcjty.rftools.blocks.relay.RelayItemBlock;
import com.mcjty.rftools.blocks.relay.RelayTileEntity;
import com.mcjty.rftools.blocks.shield.*;
import com.mcjty.rftools.blocks.storagemonitor.StorageScannerBlock;
import com.mcjty.rftools.blocks.storagemonitor.StorageScannerItemBlock;
import com.mcjty.rftools.blocks.storagemonitor.StorageScannerTileEntity;
import com.mcjty.rftools.blocks.teleporter.*;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.material.Material;

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

    public static void init() {
        monitorBlock = new RFMonitorBlock(Material.iron);
        monitorBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(monitorBlock, RFMonitorItemBlock.class, "rfMonitorBlock");
        GameRegistry.registerTileEntity(RFMonitorBlockTileEntity.class, "RFMonitorTileEntity");

        initCrafterBlocks();

        relayBlock = new RelayBlock(Material.iron);
        relayBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(relayBlock, RelayItemBlock.class, "relayBlock");
        GameRegistry.registerTileEntity(RelayTileEntity.class, "RelayTileEntity");

        storageScannerBlock = new StorageScannerBlock(Material.iron);
        storageScannerBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(storageScannerBlock, StorageScannerItemBlock.class, "storageScannerBlock");
        GameRegistry.registerTileEntity(StorageScannerTileEntity.class, "StorageScannerTileEntity");

        initTeleporterBlocks();
        initEndergenicBlocks();
        initLogicBlocks();
        initShieldBlocks();
        initBaseBlocks();
    }

    private static void initBaseBlocks() {
        machineFrame = new MachineFrame(Material.iron);
        machineFrame.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(machineFrame, "machineFrame");

        machineBase = new MachineBase(Material.iron);
        machineBase.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(machineBase, "machineBase");
    }

    private static void initCrafterBlocks() {
        crafterBlock1 = new CrafterBlock(Material.iron, "crafterBlock1", "machineCrafter1", CrafterBlockTileEntity1.class);
        crafterBlock1.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(crafterBlock1, CrafterItemBlock.class, "crafterBlock1");
        crafterBlock2 = new CrafterBlock(Material.iron, "crafterBlock2", "machineCrafter2", CrafterBlockTileEntity2.class);
        crafterBlock2.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(crafterBlock2, CrafterItemBlock.class, "crafterBlock2");
        crafterBlock3 = new CrafterBlock(Material.iron, "crafterBlock3", "machineCrafter3", CrafterBlockTileEntity3.class);
        crafterBlock3.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(crafterBlock3, CrafterItemBlock.class, "crafterBlock3");
        GameRegistry.registerTileEntity(CrafterBlockTileEntity1.class, "CrafterTileEntity1");
        GameRegistry.registerTileEntity(CrafterBlockTileEntity2.class, "CrafterTileEntity2");
        GameRegistry.registerTileEntity(CrafterBlockTileEntity3.class, "CrafterTileEntity3");
    }

    private static void initShieldBlocks() {
        shieldBlock = new ShieldBlock(Material.iron);
        shieldBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(shieldBlock, ShieldItemBlock.class, "shieldBlock");
        GameRegistry.registerTileEntity(ShieldTileEntity.class, "ShieldTileEntity");

        invisibleShieldBlock = new InvisibleShieldBlock(Material.portal);
        invisibleShieldBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(invisibleShieldBlock, "invisibleShieldBlock");

        shieldBlockNOpaquePass1 = new ShieldBlockNOpaquePass1(Material.portal);
        shieldBlockNOpaquePass1.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(shieldBlockNOpaquePass1, "shieldBlockNOpaquePass1");
        shieldBlockNOpaquePass0 = new ShieldBlockNOpaquePass0(Material.portal);
        shieldBlockNOpaquePass0.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(shieldBlockNOpaquePass0, "shieldBlockNOpaquePass0");
        shieldBlockOpaquePass1 = new ShieldBlockOpaquePass1(Material.portal);
        shieldBlockOpaquePass1.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(shieldBlockOpaquePass1, "shieldBlockOpaquePass1");
        shieldBlockOpaquePass0 = new ShieldBlockOpaquePass0(Material.portal);
        shieldBlockOpaquePass0.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(shieldBlockOpaquePass0, "shieldBlockOpaquePass0");
        shieldBlockNOpaquePass1NN = new ShieldBlockNOpaquePass1NN(Material.portal);
        shieldBlockNOpaquePass1NN.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(shieldBlockNOpaquePass1NN, "shieldBlockNOpaquePass1NN");
        shieldBlockNOpaquePass0NN = new ShieldBlockNOpaquePass0NN(Material.portal);
        shieldBlockNOpaquePass0NN.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(shieldBlockNOpaquePass0NN, "shieldBlockNOpaquePass0NN");
        shieldBlockOpaquePass1NN = new ShieldBlockOpaquePass1NN(Material.portal);
        shieldBlockOpaquePass1NN.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(shieldBlockOpaquePass1NN, "shieldBlockOpaquePass1NN");
        shieldBlockOpaquePass0NN = new ShieldBlockOpaquePass0NN(Material.portal);
        shieldBlockOpaquePass0NN.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(shieldBlockOpaquePass0NN, "shieldBlockOpaquePass0NN");

        solidShieldBlock = new SolidShieldBlock(Material.portal);
        solidShieldBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(solidShieldBlock, "solidShieldBlock");
        GameRegistry.registerTileEntity(ShieldBlockTileEntity.class, "ShieldBlockTileEntity");
        shieldTemplateBlock = new ShieldTemplateBlock(Material.glass);
        shieldTemplateBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(shieldTemplateBlock, "shieldTemplateBlock");
    }

    private static void initLogicBlocks() {
        enderMonitorBlock = new EnderMonitorBlock(Material.iron);
        enderMonitorBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(enderMonitorBlock, EnderMonitorItemBlock.class, "enderMonitorBlock");
        GameRegistry.registerTileEntity(EnderMonitorTileEntity.class, "EnderMonitorTileEntity");

        sequencerBlock = new SequencerBlock(Material.iron);
        sequencerBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(sequencerBlock, SequencerItemBlock.class, "sequencerBlock");
        GameRegistry.registerTileEntity(SequencerTileEntity.class, "SequencerTileEntity");

        timerBlock = new TimerBlock(Material.iron);
        timerBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(timerBlock, TimerItemBlock.class, "timerBlock");
        GameRegistry.registerTileEntity(TimerTileEntity.class, "TimerTileEntity");
    }

    private static void initEndergenicBlocks() {
        endergenicBlock = new EndergenicBlock(Material.iron);
        endergenicBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(endergenicBlock, EndergenicItemBlock.class, "endergenicBlock");
        GameRegistry.registerTileEntity(EndergenicTileEntity.class, "EndergenicTileEntity");

        pearlInjectorBlock = new PearlInjectorBlock(Material.iron);
        pearlInjectorBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(pearlInjectorBlock, PearlInjectorItemBlock.class, "pearlInjectorBlock");
        GameRegistry.registerTileEntity(PearlInjectorTileEntity.class, "PearlInjectorTileEntity");
    }

    private static void initTeleporterBlocks() {
        matterTransmitterBlock = new MatterTransmitterBlock(Material.iron);
        matterTransmitterBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(matterTransmitterBlock, "matterTransmitterBlock");
        GameRegistry.registerTileEntity(MatterTransmitterTileEntity.class, "MatterTransmitterTileEntity");
        matterReceiverBlock = new MatterReceiverBlock(Material.iron);
        matterReceiverBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(matterReceiverBlock, MatterReceiverItemBlock.class, "matterReceiverBlock");
        GameRegistry.registerTileEntity(MatterReceiverTileEntity.class, "MatterReceiverTileEntity");
        dialingDeviceBlock = new DialingDeviceBlock(Material.iron);
        dialingDeviceBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(dialingDeviceBlock, DialingDeviceItemBlock.class, "dialingDeviceBlock");
        GameRegistry.registerTileEntity(DialingDeviceTileEntity.class, "DialingDeviceTileEntity");
        destinationAnalyzerBlock = new DestinationAnalyzerBlock(Material.iron);
        destinationAnalyzerBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(destinationAnalyzerBlock, "destinationAnalyzerBlock");
        teleportBeamBlock = new TeleportBeamBlock(Material.portal);
        GameRegistry.registerBlock(teleportBeamBlock, "teleportBeamBlock");
    }
}
