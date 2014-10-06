package com.mcjty.rftools.blocks;

import com.mcjty.rftools.blocks.crafter.*;
import com.mcjty.rftools.blocks.monitor.RFMonitorBlock;
import com.mcjty.rftools.blocks.monitor.RFMonitorBlockTileEntity;
import com.mcjty.rftools.blocks.relay.RelayBlock;
import com.mcjty.rftools.blocks.relay.RelayTileEntity;
import com.mcjty.rftools.blocks.storagemonitor.StorageScannerBlock;
import com.mcjty.rftools.blocks.storagemonitor.StorageScannerTileEntity;
import com.mcjty.rftools.blocks.teleporter.DestinationAnalyzerBlock;
import com.mcjty.rftools.blocks.teleporter.DialingDeviceBlock;
import com.mcjty.rftools.blocks.teleporter.MatterReceiverBlock;
import com.mcjty.rftools.blocks.teleporter.MatterTransmitterBlock;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public final class ModBlocks {
    public static MachineFrame machineFrame;

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

    public static final void init() {
        monitorBlock = new RFMonitorBlock(Material.iron);
        monitorBlock.setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerBlock(monitorBlock, "rfMonitorBlock");
        GameRegistry.registerTileEntity(RFMonitorBlockTileEntity.class, "RFMonitorTileEntity");

        crafterBlock1 = new CrafterBlock(Material.iron, "crafterBlock1", "machineCrafter1", CrafterBlockTileEntity1.class);
        crafterBlock1.setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerBlock(crafterBlock1, "crafterBlock1");
        crafterBlock2 = new CrafterBlock(Material.iron, "crafterBlock2", "machineCrafter2", CrafterBlockTileEntity2.class);
        crafterBlock2.setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerBlock(crafterBlock2, "crafterBlock2");
        crafterBlock3 = new CrafterBlock(Material.iron, "crafterBlock3", "machineCrafter3", CrafterBlockTileEntity3.class);
        crafterBlock3.setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerBlock(crafterBlock3, "crafterBlock3");
        GameRegistry.registerTileEntity(CrafterBlockTileEntity1.class, "CrafterTileEntity1");
        GameRegistry.registerTileEntity(CrafterBlockTileEntity2.class, "CrafterTileEntity2");
        GameRegistry.registerTileEntity(CrafterBlockTileEntity3.class, "CrafterTileEntity3");

        relayBlock = new RelayBlock(Material.iron);
        relayBlock.setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerBlock(relayBlock, "relayBlock");
        GameRegistry.registerTileEntity(RelayTileEntity.class, "RelayTileEntity");

        storageScannerBlock = new StorageScannerBlock(Material.iron);
        storageScannerBlock.setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerBlock(storageScannerBlock, "storageScannerBlock");
        GameRegistry.registerTileEntity(StorageScannerTileEntity.class, "StorageScannerTileEntity");

        matterTransmitterBlock = new MatterTransmitterBlock(Material.iron);
        matterTransmitterBlock.setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerBlock(matterTransmitterBlock, "matterTransmitterBlock");
        matterReceiverBlock = new MatterReceiverBlock(Material.iron);
        matterReceiverBlock.setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerBlock(matterReceiverBlock, "matterReceiverBlock");
        dialingDeviceBlock = new DialingDeviceBlock(Material.iron);
        dialingDeviceBlock.setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerBlock(dialingDeviceBlock, "dialingDeviceBlock");
        destinationAnalyzerBlock = new DestinationAnalyzerBlock(Material.iron);
        destinationAnalyzerBlock.setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerBlock(destinationAnalyzerBlock, "destinationAnalyzerBlock");

        machineFrame = new MachineFrame(Material.iron);
        machineFrame.setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerBlock(machineFrame, "machineFrame");
    }
}
