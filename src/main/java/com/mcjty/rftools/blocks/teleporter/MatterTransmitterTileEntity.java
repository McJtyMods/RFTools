package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;

public class MatterTransmitterTileEntity extends GenericEnergyHandlerTileEntity {

    public static final int MAXENERGY = 1000000;
    public static final int RECEIVEPERTICK = 1000;

    public MatterTransmitterTileEntity() {
        super(MAXENERGY, RECEIVEPERTICK);
    }
}
