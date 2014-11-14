package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;

public class DimletResearcherBlock extends GenericContainerBlock {

    public DimletResearcherBlock(Material material) {
        super(material, DimletResearcherTileEntity.class);
        setBlockName("dimletResearcherBlock");
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_DIMLET_RESEARCHER;
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineDimletResearcher";
    }
}
