package com.mcjty.rftools.gui;

import com.mcjty.container.EmptyContainer;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.crafter.CrafterBlockTileEntity3;
import com.mcjty.rftools.blocks.crafter.CrafterContainer;
import com.mcjty.rftools.blocks.crafter.GuiCrafter;
import com.mcjty.rftools.blocks.endergen.EndergenicBlock;
import com.mcjty.rftools.blocks.endergen.EndergenicTileEntity;
import com.mcjty.rftools.blocks.endergen.GuiEndergenic;
import com.mcjty.rftools.blocks.monitor.GuiRFMonitor;
import com.mcjty.rftools.blocks.monitor.RFMonitorBlock;
import com.mcjty.rftools.blocks.monitor.RFMonitorBlockTileEntity;
import com.mcjty.rftools.blocks.relay.GuiRelay;
import com.mcjty.rftools.blocks.relay.RelayBlock;
import com.mcjty.rftools.blocks.relay.RelayTileEntity;
import com.mcjty.rftools.blocks.storagemonitor.GuiStorageScanner;
import com.mcjty.rftools.blocks.storagemonitor.StorageScannerTileEntity;
import com.mcjty.rftools.blocks.teleporter.*;
import com.mcjty.rftools.items.manual.GuiRFToolsManual;
import com.mcjty.rftools.items.netmonitor.GuiNetworkMonitor;
import com.mcjty.rftools.items.teleportprobe.GuiTeleportProbe;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiProxy implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int guiid, EntityPlayer entityPlayer, World world, int x, int y, int z) {
        if (guiid == RFTools.GUI_LIST_BLOCKS || guiid == RFTools.GUI_TELEPORTPROBE ||
                guiid == RFTools.GUI_MANUAL || guiid == RFTools.GUI_RF_MONITOR || guiid == RFTools.GUI_RELAY) {
            return null;
        } else if (guiid == RFTools.GUI_CRAFTER) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof CrafterBlockTileEntity3) {
                CrafterBlockTileEntity3 crafterBlockTileEntity = (CrafterBlockTileEntity3) te;
                return new CrafterContainer(entityPlayer, crafterBlockTileEntity);
            }
        } else if (guiid == RFTools.GUI_STORAGE_SCANNER) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof StorageScannerTileEntity) {
                StorageScannerTileEntity storageScannerTileEntity = (StorageScannerTileEntity) te;
                return new EmptyContainer<StorageScannerTileEntity>(entityPlayer, storageScannerTileEntity);
            }
        } else if (guiid == RFTools.GUI_MATTER_TRANSMITTER) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof MatterTransmitterTileEntity) {
                MatterTransmitterTileEntity matterTransmitterTileEntity = (MatterTransmitterTileEntity) te;
                return new EmptyContainer<MatterTransmitterTileEntity>(entityPlayer, matterTransmitterTileEntity);
            }
        } else if (guiid == RFTools.GUI_MATTER_RECEIVER) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof MatterReceiverTileEntity) {
                MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) te;
                return new EmptyContainer<MatterReceiverTileEntity>(entityPlayer, matterReceiverTileEntity);
            }
        } else if (guiid == RFTools.GUI_ENDERGENIC) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof EndergenicTileEntity) {
                EndergenicTileEntity endergenicTileEntity = (EndergenicTileEntity) te;
                return new EmptyContainer<EndergenicTileEntity>(entityPlayer, endergenicTileEntity);
            }
        } else if (guiid == RFTools.GUI_DIALING_DEVICE) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof DialingDeviceTileEntity) {
                DialingDeviceTileEntity dialingDeviceTileEntity = (DialingDeviceTileEntity) te;
                return new EmptyContainer<DialingDeviceTileEntity>(entityPlayer, dialingDeviceTileEntity);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int guiid, EntityPlayer entityPlayer, World world, int x, int y, int z) {
        if (guiid == RFTools.GUI_LIST_BLOCKS) {
            return new GuiNetworkMonitor();
        } else if (guiid == RFTools.GUI_TELEPORTPROBE) {
            return new GuiTeleportProbe();
        } else if (guiid == RFTools.GUI_MANUAL) {
            return new GuiRFToolsManual();
        } else if (guiid == RFTools.GUI_RF_MONITOR) {
            Block block = world.getBlock(x, y, z);
            TileEntity te = world.getTileEntity(x, y, z);

            if (block != null && block instanceof  RFMonitorBlock && te instanceof RFMonitorBlockTileEntity) {
                RFMonitorBlockTileEntity monitorBlockTileEntity = (RFMonitorBlockTileEntity) te;
                RFMonitorBlock monitorBlock = (RFMonitorBlock) block;
                return new GuiRFMonitor(monitorBlock, monitorBlockTileEntity);
            }
        } else if (guiid == RFTools.GUI_CRAFTER) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof CrafterBlockTileEntity3) {
                CrafterBlockTileEntity3 crafterBlockTileEntity = (CrafterBlockTileEntity3) te;
                CrafterContainer crafterContainer = new CrafterContainer(entityPlayer, crafterBlockTileEntity);
                return new GuiCrafter(crafterBlockTileEntity, crafterContainer);
            }
        } else if (guiid == RFTools.GUI_STORAGE_SCANNER) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof StorageScannerTileEntity) {
                StorageScannerTileEntity storageScannerTileEntity = (StorageScannerTileEntity) te;
                EmptyContainer<StorageScannerTileEntity> storageScannerContainer = new EmptyContainer<StorageScannerTileEntity>(entityPlayer, storageScannerTileEntity);
                return new GuiStorageScanner(storageScannerTileEntity, storageScannerContainer);
            }
        } else if (guiid == RFTools.GUI_MATTER_TRANSMITTER) {
            TileEntity te = world.getTileEntity(x, y, z);

            if (te instanceof MatterTransmitterTileEntity) {
                MatterTransmitterTileEntity matterTransmitterTileEntity = (MatterTransmitterTileEntity) te;
                EmptyContainer<MatterTransmitterTileEntity> matterTransmitterContainer = new EmptyContainer<MatterTransmitterTileEntity>(entityPlayer, matterTransmitterTileEntity);
                return new GuiMatterTransmitter(matterTransmitterTileEntity, matterTransmitterContainer);
            }
        } else if (guiid == RFTools.GUI_MATTER_RECEIVER) {
            TileEntity te = world.getTileEntity(x, y, z);

            if (te instanceof MatterReceiverTileEntity) {
                MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) te;
                EmptyContainer<MatterReceiverTileEntity> matterReceiverContainer = new EmptyContainer<MatterReceiverTileEntity>(entityPlayer, matterReceiverTileEntity);
                return new GuiMatterReceiver(matterReceiverTileEntity, matterReceiverContainer);
            }
        } else if (guiid == RFTools.GUI_ENDERGENIC) {
            Block block = world.getBlock(x, y, z);
            TileEntity te = world.getTileEntity(x, y, z);

            if (block != null && block instanceof EndergenicBlock && te instanceof EndergenicTileEntity) {
                EndergenicTileEntity endergenicTileEntity = (EndergenicTileEntity) te;
                EmptyContainer<EndergenicTileEntity> endergenicContainer = new EmptyContainer<EndergenicTileEntity>(entityPlayer, endergenicTileEntity);
                return new GuiEndergenic(endergenicTileEntity, endergenicContainer);
            }
        } else if (guiid == RFTools.GUI_RELAY) {
            Block block = world.getBlock(x, y, z);
            TileEntity te = world.getTileEntity(x, y, z);

            if (block != null && block instanceof RelayBlock && te instanceof RelayTileEntity) {
                RelayTileEntity relayTileEntity = (RelayTileEntity) te;
                return new GuiRelay(relayTileEntity);
            }
        } else if (guiid == RFTools.GUI_DIALING_DEVICE) {
            TileEntity te = world.getTileEntity(x, y, z);

            if (te instanceof DialingDeviceTileEntity) {
                DialingDeviceTileEntity dialingDeviceTileEntity = (DialingDeviceTileEntity) te;
                EmptyContainer<DialingDeviceTileEntity> dialingDeviceContainer = new EmptyContainer<DialingDeviceTileEntity>(entityPlayer, dialingDeviceTileEntity);
                return new GuiDialingDevice(dialingDeviceTileEntity, dialingDeviceContainer);
            }
        }
        return null;
    }
}
