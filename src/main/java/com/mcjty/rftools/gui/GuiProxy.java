package com.mcjty.rftools.gui;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.RFMonitorBlock;
import com.mcjty.rftools.items.NetworkMonitorItem;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class GuiProxy implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int guiid, EntityPlayer entityPlayer, World world, int x, int y, int z) {
        System.out.println("#### com.mcjty.rftools.gui.GuiProxy.getServerGuiElement");
        if (guiid == RFTools.GUI_LIST_BLOCKS || guiid == RFTools.GUI_RF_MONITOR) {
            return null;
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int guiid, EntityPlayer entityPlayer, World world, int x, int y, int z) {
        System.out.println("#### com.mcjty.rftools.gui.GuiProxy.getClientGuiElement");
        if (guiid == RFTools.GUI_LIST_BLOCKS) {
            NetworkMonitorItem item = getNetworkMonitorItem(entityPlayer);
            if (item != null) {
                System.out.println("com.mcjty.rftools.gui.GuiProxy.getClientGuiElement");
                return new GuiBlockList(item);

            }
        } else if (guiid == RFTools.GUI_RF_MONITOR) {
            Block block = world.getBlock(x, y, z);
            System.out.println("block = " + block);
            if (block != null && block instanceof  RFMonitorBlock) {
                RFMonitorBlock monitorBlock = (RFMonitorBlock) block;
                return new GuiRFMonitor(monitorBlock);
            }
        }
        return null;
    }

    private NetworkMonitorItem getNetworkMonitorItem(EntityPlayer entityPlayer) {
        ItemStack stack = entityPlayer.getHeldItem();
        if (stack != null) {
            Item item = stack.getItem();
            if (item != null) {
                try {
                    NetworkMonitorItem networkMonitorItem = (NetworkMonitorItem) item;
                    return networkMonitorItem;
                } catch (ClassCastException e) {
                    RFTools.logError("Held item is not a network monitor item!");
                }
            }
        }
        RFTools.logError("Held item is not a network monitor item!");
        return null;
    }

}
