package com.mcjty.rftools.gui;

import com.mcjty.container.GenericBlock;
import com.mcjty.rftools.RFTools;
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
                guiid == RFTools.GUI_MANUAL_MAIN || guiid == RFTools.GUI_MANUAL_DIMENSION) {
            return null;
        }

        Block block = world.getBlock(x, y, z);
        if (block instanceof GenericBlock) {
            GenericBlock genericBlock = (GenericBlock) block;
            TileEntity te = world.getTileEntity(x, y, z);
            return genericBlock.createServerContainer(entityPlayer, te);
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
        } else if (guiid == RFTools.GUI_MANUAL_MAIN) {
            return new GuiRFToolsManual(GuiRFToolsManual.MANUAL_MAIN);
        } else if (guiid == RFTools.GUI_MANUAL_DIMENSION) {
            return new GuiRFToolsManual(GuiRFToolsManual.MANUAL_DIMENSION);
        }

        Block block = world.getBlock(x, y, z);
        if (block instanceof GenericBlock) {
            GenericBlock genericBlock = (GenericBlock) block;
            TileEntity te = world.getTileEntity(x, y, z);
            return genericBlock.createClientGui(entityPlayer, te);
        }
        return null;
    }
}
