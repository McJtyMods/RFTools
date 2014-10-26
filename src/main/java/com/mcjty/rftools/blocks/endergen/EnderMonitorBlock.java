package com.mcjty.rftools.blocks.endergen;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.logic.LogicSlabBlock;
import net.minecraft.block.material.Material;

public class EnderMonitorBlock extends LogicSlabBlock {

    public EnderMonitorBlock(Material material) {
        super(material, "enderMonitorBlock", EnderMonitorTileEntity.class);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_ENDERMONITOR;
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineEnderMonitorTop";
    }
}
