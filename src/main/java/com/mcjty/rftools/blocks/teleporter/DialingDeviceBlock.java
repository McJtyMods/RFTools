package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.container.EmptyContainer;
import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.Infusable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class DialingDeviceBlock extends GenericContainerBlock implements Infusable {

    public DialingDeviceBlock() {
        super(Material.iron, DialingDeviceTileEntity.class);
        setBlockName("dialingDeviceBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineDialingDevice";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "With the dialing device you can 'dial-up' any");
            list.add(EnumChatFormatting.WHITE + "nearby matter transmitter to any matter receiver");
            list.add(EnumChatFormatting.WHITE + "in the Minecraft universe. This requires power.");
            list.add(EnumChatFormatting.WHITE + "If a Destination Analyzer is adjacent to this block");
            list.add(EnumChatFormatting.WHITE + "you will also be able to check if the destination");
            list.add(EnumChatFormatting.WHITE + "has enough power to be safe.");
            list.add(EnumChatFormatting.YELLOW + "Infusing bonus: reduced power consumption.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }


    @Override
    public int getGuiID() {
        return RFTools.GUI_DIALING_DEVICE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        DialingDeviceTileEntity dialingDeviceTileEntity = (DialingDeviceTileEntity) tileEntity;
        EmptyContainer dialingDeviceContainer = new EmptyContainer(entityPlayer);
        return new GuiDialingDevice(dialingDeviceTileEntity, dialingDeviceContainer);
    }
}
