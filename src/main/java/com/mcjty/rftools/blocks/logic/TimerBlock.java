package com.mcjty.rftools.blocks.logic;

import com.mcjty.container.EmptyContainer;
import com.mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class TimerBlock extends LogicSlabBlock {

    public TimerBlock(Material material) {
        super(material, "timerBlock", TimerTileEntity.class);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_TIMER;
    }

    @Override
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        TimerTileEntity timerTileEntity = (TimerTileEntity) tileEntity;
        return new GuiTimer(timerTileEntity, new EmptyContainer(entityPlayer));
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineTimerTop";
    }
}
