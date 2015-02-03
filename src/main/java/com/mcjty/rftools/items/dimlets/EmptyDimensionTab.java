package com.mcjty.rftools.items.dimlets;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class EmptyDimensionTab extends Item {

    public EmptyDimensionTab() {
        setMaxStackSize(16);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        list.add(EnumChatFormatting.YELLOW + "Put this empty dimension tab in a 'Dimension Enscriber'");
        list.add(EnumChatFormatting.YELLOW + "where you can construct a dimension using dimlets");
    }
}
