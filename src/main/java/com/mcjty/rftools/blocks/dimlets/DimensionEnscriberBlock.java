package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;

public class DimensionEnscriberBlock extends GenericContainerBlock {

    public DimensionEnscriberBlock(Material material) {
        super(material, DimensionEnscriberTileEntity.class);
        setBlockName("dimensionEnscriberBlock");
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_DIMENSION_ENSCRIBER;
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineDimensionEnscriber";
    }
}
