package com.example.rftools.gui;

import com.example.rftools.RFTools;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GuiProxy implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int guiid, EntityPlayer entityPlayer, World world, int x, int y, int z) {
        if (guiid == RFTools.GUI_LIST_BLOCKS) {
//            return new ContainerItem(entityPlayer, entityPlayer.inventory, new InventoryItem(entityPlayer.getHeldItem()));
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int guiid, EntityPlayer entityPlayer, World world, int x, int y, int z) {
        if (guiid == RFTools.GUI_LIST_BLOCKS) {
//            return new GuiItemInventory(new ContainerItem(entityPlayer, entityPlayer.inventory, new InventoryItem(entityPlayer.getHeldItem())));
        }
        return null;
    }
}
