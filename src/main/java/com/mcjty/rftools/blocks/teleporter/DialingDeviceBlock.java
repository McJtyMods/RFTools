package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;

public class DialingDeviceBlock extends GenericContainerBlock {

    public DialingDeviceBlock(Material material) {
        super(material, DialingDeviceTileEntity.class);
        setBlockName("dialingDeviceBlock");
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineDialingDevice";
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_DIALING_DEVICE;
    }

}
