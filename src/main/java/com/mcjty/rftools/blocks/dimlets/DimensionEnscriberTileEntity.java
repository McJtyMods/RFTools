package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;

public class DimensionEnscriberTileEntity extends GenericEnergyHandlerTileEntity {

    public DimensionEnscriberTileEntity() {
        super(DimletConfiguration.ENSCRIBER_MAXENERGY, DimletConfiguration.ENSCRIBER_RECEIVEPERTICK);
    }
}
