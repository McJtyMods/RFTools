package com.mcjty.rftools.gui;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.CrafterBlockTileEntity;
import com.mcjty.rftools.blocks.CrafterContainer;
import com.mcjty.rftools.blocks.RFMonitorBlock;
import com.mcjty.rftools.blocks.RFMonitorBlockTileEntity;
import com.mcjty.rftools.items.NetworkMonitorItem;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiProxy implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int guiid, EntityPlayer entityPlayer, World world, int x, int y, int z) {
        System.out.println("#### com.mcjty.rftools.gui.GuiProxy.getServerGuiElement");
        if (guiid == RFTools.GUI_LIST_BLOCKS || guiid == RFTools.GUI_RF_MONITOR) {
            return null;
        } else if (guiid == RFTools.GUI_CRAFTER) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof CrafterBlockTileEntity) {
                CrafterBlockTileEntity crafterBlockTileEntity = (CrafterBlockTileEntity) te;
                return new CrafterContainer(entityPlayer, crafterBlockTileEntity, 0, 0);
            }
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
                return new GuiNetworkMonitor(item);

            }
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
            if (te instanceof CrafterBlockTileEntity) {
                CrafterBlockTileEntity crafterBlockTileEntity = (CrafterBlockTileEntity) te;
                CrafterContainer testContainer = new CrafterContainer(entityPlayer, crafterBlockTileEntity, 184, 184);
                return new GuiCrafter(testContainer);
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
