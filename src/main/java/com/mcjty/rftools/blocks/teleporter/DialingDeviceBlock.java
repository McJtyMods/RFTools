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
import net.minecraft.tileentity.TileEntity;

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
