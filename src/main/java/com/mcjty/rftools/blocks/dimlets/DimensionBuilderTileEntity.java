package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;

public class DimensionBuilderTileEntity extends GenericEnergyHandlerTileEntity {

    public DimensionBuilderTileEntity() {
        super(DimletConfiguration.BUILDER_MAXENERGY, DimletConfiguration.BUILDER_RECEIVEPERTICK);
    }
}
