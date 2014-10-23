package com.mcjty.rftools.blocks.logic;

import com.mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;
import net.minecraft.util.IIcon;

public class SequencerBlock extends LogicSlabBlock {

    private IIcon iconTop;

    public SequencerBlock(Material material) {
        super(material, "sequencerBlock", SequencerTileEntity.class);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_SEQUENCER;
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineSequencerTop";
    }
}
