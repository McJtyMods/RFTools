package com.mcjty.rftools.blocks.environmental;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;

public class EnvironmentalControllerTileEntity extends GenericEnergyHandlerTileEntity {

    public EnvironmentalControllerTileEntity() {
        super(EnvironmentalConfiguration.ENVIRONMENTAL_MAXENERGY, EnvironmentalConfiguration.ENVIRONMENTAL_RECEIVEPERTICK);
    }
}
