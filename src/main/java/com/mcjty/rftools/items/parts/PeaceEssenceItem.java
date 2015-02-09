package com.mcjty.rftools.items.parts;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class PeaceEssenceItem extends Item {
    public PeaceEssenceItem() {
        setMaxStackSize(64);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This essence item is the main ingredient for");
            list.add(EnumChatFormatting.WHITE + "the peaceful dimlet in the Dimlet Workbench.");
            list.add(EnumChatFormatting.WHITE + "Getting this essence is somewhat harder though.");
        } else {
            list.add(EnumChatFormatting.WHITE + "Press Shift for more");
        }
    }
}
