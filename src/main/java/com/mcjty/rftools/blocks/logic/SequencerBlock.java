package com.mcjty.rftools.blocks.logic;

import com.mcjty.container.EmptyContainer;
import com.mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class SequencerBlock extends LogicSlabBlock {

    public SequencerBlock(Material material) {
        super(material, "sequencerBlock", SequencerTileEntity.class);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_SEQUENCER;
    }

    @Override
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        SequencerTileEntity sequencerTileEntity = (SequencerTileEntity) tileEntity;
        return new GuiSequencer(sequencerTileEntity, new EmptyContainer(entityPlayer));
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineSequencerTop";
    }
}
