package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;

public class DimensionChestBlock extends GenericContainerBlock {

    public DimensionChestBlock(Material material) {
        super(material, DimensionChestTileEntity.class);
        setBlockName("dimensionChestBlock");
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_DIMENSION_CHEST;
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineDimensionChest";
    }
}
