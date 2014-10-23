package com.mcjty.rftools.blocks.logic;

import com.mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;

public class TimerBlock extends LogicSlabBlock {

    public TimerBlock(Material material) {
        super(material, "timerBlock", TimerTileEntity.class);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_TIMER;
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineTimerTop";
    }
}
