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
    public static VisibleShieldBlock visibleShieldBlock;
    public static SolidShieldBlock solidShieldBlock;
    public static ShieldTemplateBlock shieldTemplateBlock;

    public static final void init() {
        monitorBlock = new RFMonitorBlock(Material.iron);
        monitorBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(monitorBlock, RFMonitorItemBlock.class, "rfMonitorBlock");
        GameRegistry.registerTileEntity(RFMonitorBlockTileEntity.class, "RFMonitorTileEntity");

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

        relayBlock = new RelayBlock(Material.iron);
        relayBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(relayBlock, RelayItemBlock.class, "relayBlock");
        GameRegistry.registerTileEntity(RelayTileEntity.class, "RelayTileEntity");

        storageScannerBlock = new StorageScannerBlock(Material.iron);
        storageScannerBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(storageScannerBlock, StorageScannerItemBlock.class, "storageScannerBlock");
        GameRegistry.registerTileEntity(StorageScannerTileEntity.class, "StorageScannerTileEntity");

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

        endergenicBlock = new EndergenicBlock(Material.iron);
        endergenicBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(endergenicBlock, EndergenicItemBlock.class, "endergenicBlock");
        GameRegistry.registerTileEntity(EndergenicTileEntity.class, "EndergenicTileEntity");

        pearlInjectorBlock = new PearlInjectorBlock(Material.iron);
        pearlInjectorBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(pearlInjectorBlock, PearlInjectorItemBlock.class, "pearlInjectorBlock");
        GameRegistry.registerTileEntity(PearlInjectorTileEntity.class, "PearlInjectorTileEntity");

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

        shieldBlock = new ShieldBlock(Material.iron);
        shieldBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(shieldBlock, ShieldItemBlock.class, "shieldBlock");
        GameRegistry.registerTileEntity(ShieldTileEntity.class, "ShieldTileEntity");

        invisibleShieldBlock = new InvisibleShieldBlock(Material.portal);
        invisibleShieldBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(invisibleShieldBlock, "invisibleShieldBlock");
        visibleShieldBlock = new VisibleShieldBlock(Material.portal);
        visibleShieldBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(visibleShieldBlock, "visibleShieldBlock");
        solidShieldBlock = new SolidShieldBlock(Material.portal);
        solidShieldBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(solidShieldBlock, "solidShieldBlock");
        GameRegistry.registerTileEntity(CamoBlockShieldTileEntity.class, "CamoBlockShieldTileEntity");
        shieldTemplateBlock = new ShieldTemplateBlock(Material.glass);
        shieldTemplateBlock.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(shieldTemplateBlock, "shieldTemplateBlock");

        machineFrame = new MachineFrame(Material.iron);
        machineFrame.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(machineFrame, "machineFrame");

        machineBase = new MachineBase(Material.iron);
        machineBase.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(machineBase, "machineBase");
    }
}
