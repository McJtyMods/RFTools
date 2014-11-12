package com.mcjty.rftools.gui;

import com.mcjty.container.EmptyContainer;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.crafter.CrafterBlockTileEntity3;
import com.mcjty.rftools.blocks.crafter.CrafterContainer;
import com.mcjty.rftools.blocks.crafter.GuiCrafter;
import com.mcjty.rftools.blocks.endergen.*;
import com.mcjty.rftools.blocks.logic.*;
import com.mcjty.rftools.blocks.monitor.GuiRFMonitor;
import com.mcjty.rftools.blocks.monitor.RFMonitorBlock;
import com.mcjty.rftools.blocks.monitor.RFMonitorBlockTileEntity;
import com.mcjty.rftools.blocks.relay.GuiRelay;
import com.mcjty.rftools.blocks.relay.RelayBlock;
import com.mcjty.rftools.blocks.relay.RelayTileEntity;
import com.mcjty.rftools.blocks.shield.GuiShield;
import com.mcjty.rftools.blocks.shield.ShieldContainer;
import com.mcjty.rftools.blocks.shield.ShieldTileEntity;
import com.mcjty.rftools.blocks.storagemonitor.GuiStorageScanner;
import com.mcjty.rftools.blocks.storagemonitor.StorageScannerTileEntity;
import com.mcjty.rftools.blocks.teleporter.*;
import com.mcjty.rftools.items.devdelight.GuiDevelopersDelight;
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
        if (guiid == RFTools.GUI_LIST_BLOCKS || guiid == RFTools.GUI_TELEPORTPROBE || guiid == RFTools.GUI_DEVELOPERS_DELIGHT ||
                guiid == RFTools.GUI_MANUAL || guiid == RFTools.GUI_RF_MONITOR || guiid == RFTools.GUI_RELAY ||
                guiid == RFTools.GUI_SEQUENCER || guiid == RFTools.GUI_TIMER || guiid == RFTools.GUI_ENDERMONITOR) {
            return null;
        } else if (guiid == RFTools.GUI_CRAFTER) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof CrafterBlockTileEntity3) {
                CrafterBlockTileEntity3 crafterBlockTileEntity = (CrafterBlockTileEntity3) te;
                return new CrafterContainer(entityPlayer, crafterBlockTileEntity);
            }
        } else if (guiid == RFTools.GUI_SHIELD) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof ShieldTileEntity) {
                ShieldTileEntity shieldTileEntity = (ShieldTileEntity) te;
                return new ShieldContainer(entityPlayer, shieldTileEntity);
            }
        } else if (guiid == RFTools.GUI_PEARL_INJECTOR) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof PearlInjectorTileEntity) {
                PearlInjectorTileEntity pearlInjectorTileEntity = (PearlInjectorTileEntity) te;
                return new PearlInjectorContainer(entityPlayer, pearlInjectorTileEntity);
            }
        } else if (guiid == RFTools.GUI_STORAGE_SCANNER) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof StorageScannerTileEntity) {
                return new EmptyContainer<StorageScannerTileEntity>(entityPlayer);
            }
        } else if (guiid == RFTools.GUI_MATTER_TRANSMITTER) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof MatterTransmitterTileEntity) {
                return new EmptyContainer<MatterTransmitterTileEntity>(entityPlayer);
            }
        } else if (guiid == RFTools.GUI_MATTER_RECEIVER) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof MatterReceiverTileEntity) {
                return new EmptyContainer<MatterReceiverTileEntity>(entityPlayer);
            }
        } else if (guiid == RFTools.GUI_ENDERGENIC) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof EndergenicTileEntity) {
                return new EmptyContainer<EndergenicTileEntity>(entityPlayer);
            }
        } else if (guiid == RFTools.GUI_DIALING_DEVICE) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof DialingDeviceTileEntity) {
                return new EmptyContainer<DialingDeviceTileEntity>(entityPlayer);
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
        } else if (guiid == RFTools.GUI_DEVELOPERS_DELIGHT) {
            return new GuiDevelopersDelight();
        } else if (guiid == RFTools.GUI_MANUAL) {
            return new GuiRFToolsManual();
        } else if (guiid == RFTools.GUI_RF_MONITOR) {
            Block block = world.getBlock(x, y, z);
            TileEntity te = world.getTileEntity(x, y, z);

            if (block != null && block instanceof  RFMonitorBlock && te instanceof RFMonitorBlockTileEntity) {
                RFMonitorBlockTileEntity monitorBlockTileEntity = (RFMonitorBlockTileEntity) te;
                return new GuiRFMonitor(monitorBlockTileEntity);
            }
        } else if (guiid == RFTools.GUI_SHIELD) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof ShieldTileEntity) {
                ShieldTileEntity shieldTileEntity = (ShieldTileEntity) te;
                ShieldContainer shieldContainer = new ShieldContainer(entityPlayer, shieldTileEntity);
                return new GuiShield(shieldTileEntity, shieldContainer);
            }
        } else if (guiid == RFTools.GUI_CRAFTER) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof CrafterBlockTileEntity3) {
                CrafterBlockTileEntity3 crafterBlockTileEntity = (CrafterBlockTileEntity3) te;
                CrafterContainer crafterContainer = new CrafterContainer(entityPlayer, crafterBlockTileEntity);
                return new GuiCrafter(crafterBlockTileEntity, crafterContainer);
            }
        } else if (guiid == RFTools.GUI_PEARL_INJECTOR) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof PearlInjectorTileEntity) {
                PearlInjectorTileEntity pearlInjectorTileEntity = (PearlInjectorTileEntity) te;
                PearlInjectorContainer pearlInjectorContainer = new PearlInjectorContainer(entityPlayer, pearlInjectorTileEntity);
                return new GuiPearlInjector(pearlInjectorContainer);
            }
        } else if (guiid == RFTools.GUI_STORAGE_SCANNER) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof StorageScannerTileEntity) {
                StorageScannerTileEntity storageScannerTileEntity = (StorageScannerTileEntity) te;
                EmptyContainer<StorageScannerTileEntity> storageScannerContainer = new EmptyContainer<StorageScannerTileEntity>(entityPlayer);
                return new GuiStorageScanner(storageScannerTileEntity, storageScannerContainer);
            }
        } else if (guiid == RFTools.GUI_MATTER_TRANSMITTER) {
            TileEntity te = world.getTileEntity(x, y, z);

            if (te instanceof MatterTransmitterTileEntity) {
                MatterTransmitterTileEntity matterTransmitterTileEntity = (MatterTransmitterTileEntity) te;
                EmptyContainer<MatterTransmitterTileEntity> matterTransmitterContainer = new EmptyContainer<MatterTransmitterTileEntity>(entityPlayer);
                return new GuiMatterTransmitter(matterTransmitterTileEntity, matterTransmitterContainer);
            }
        } else if (guiid == RFTools.GUI_MATTER_RECEIVER) {
            TileEntity te = world.getTileEntity(x, y, z);

            if (te instanceof MatterReceiverTileEntity) {
                MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) te;
                EmptyContainer<MatterReceiverTileEntity> matterReceiverContainer = new EmptyContainer<MatterReceiverTileEntity>(entityPlayer);
                return new GuiMatterReceiver(matterReceiverTileEntity, matterReceiverContainer);
            }
        } else if (guiid == RFTools.GUI_ENDERGENIC) {
            Block block = world.getBlock(x, y, z);
            TileEntity te = world.getTileEntity(x, y, z);

            if (block != null && block instanceof EndergenicBlock && te instanceof EndergenicTileEntity) {
                EndergenicTileEntity endergenicTileEntity = (EndergenicTileEntity) te;
                EmptyContainer<EndergenicTileEntity> endergenicContainer = new EmptyContainer<EndergenicTileEntity>(entityPlayer);
                return new GuiEndergenic(endergenicTileEntity, endergenicContainer);
            }
        } else if (guiid == RFTools.GUI_RELAY) {
            Block block = world.getBlock(x, y, z);
            TileEntity te = world.getTileEntity(x, y, z);

            if (block != null && block instanceof RelayBlock && te instanceof RelayTileEntity) {
                RelayTileEntity relayTileEntity = (RelayTileEntity) te;
                return new GuiRelay(relayTileEntity);
            }
        } else if (guiid == RFTools.GUI_SEQUENCER) {
            Block block = world.getBlock(x, y, z);
            TileEntity te = world.getTileEntity(x, y, z);

            if (block != null && block instanceof SequencerBlock && te instanceof SequencerTileEntity) {
                SequencerTileEntity sequencerTileEntity = (SequencerTileEntity) te;
                return new GuiSequencer(sequencerTileEntity);
            }
        } else if (guiid == RFTools.GUI_TIMER) {
            Block block = world.getBlock(x, y, z);
            TileEntity te = world.getTileEntity(x, y, z);

            if (block != null && block instanceof TimerBlock && te instanceof TimerTileEntity) {
                TimerTileEntity timerTileEntity = (TimerTileEntity) te;
                return new GuiTimer(timerTileEntity);
            }
        } else if (guiid == RFTools.GUI_ENDERMONITOR) {
            Block block = world.getBlock(x, y, z);
            TileEntity te = world.getTileEntity(x, y, z);

            if (block != null && block instanceof EnderMonitorBlock && te instanceof EnderMonitorTileEntity) {
                EnderMonitorTileEntity enderMonitorTileEntity = (EnderMonitorTileEntity) te;
                return new GuiEnderMonitor(enderMonitorTileEntity);
            }
        } else if (guiid == RFTools.GUI_DIALING_DEVICE) {
            TileEntity te = world.getTileEntity(x, y, z);

            if (te instanceof DialingDeviceTileEntity) {
                DialingDeviceTileEntity dialingDeviceTileEntity = (DialingDeviceTileEntity) te;
                EmptyContainer<DialingDeviceTileEntity> dialingDeviceContainer = new EmptyContainer<DialingDeviceTileEntity>(entityPlayer);
                return new GuiDialingDevice(dialingDeviceTileEntity, dialingDeviceContainer);
            }
        }
        return null;
    }
}
