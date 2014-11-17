package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;

public class DimensionBuilderBlock extends GenericContainerBlock {

    public DimensionBuilderBlock(Material material) {
        super(material, DimensionBuilderTileEntity.class);
        setBlockName("dimensionBuilderBlock");
    }

    @Override
    public int getGuiID() {
        return -1;
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineDimensionBuilder";
    }
}
