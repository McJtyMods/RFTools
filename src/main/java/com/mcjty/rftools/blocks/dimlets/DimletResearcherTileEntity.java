package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;

public class DimletResearcherTileEntity extends GenericEnergyHandlerTileEntity {

    public DimletResearcherTileEntity() {
        super(DimletConfiguration.RESEARCHER_MAXENERGY, DimletConfiguration.RESEARCHER_RECEIVEPERTICK);
    }
}
