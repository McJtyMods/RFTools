package com.mcjty.rftools.blocks.screens;

import com.mcjty.container.GenericBlock;
import com.mcjty.rftools.RFTools;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;

public class ScreenControllerBlock extends GenericBlock {

    public ScreenControllerBlock() {
        super(Material.iron, ScreenControllerTileEntity.class);
        setBlockName("screenControllerBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_SCREENCONTROLLER;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        ScreenControllerTileEntity screenControllerTileEntity = (ScreenControllerTileEntity) tileEntity;
        ScreenControllerContainer screenControllerContainer = new ScreenControllerContainer(entityPlayer, screenControllerTileEntity);
        return new GuiScreenController(screenControllerTileEntity, screenControllerContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new ScreenControllerContainer(entityPlayer, (ScreenControllerTileEntity) tileEntity);
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineScreenController";
    }
}
