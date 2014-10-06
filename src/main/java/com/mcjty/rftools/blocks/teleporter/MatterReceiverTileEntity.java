package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;

public class MatterReceiverTileEntity extends GenericEnergyHandlerTileEntity {

    public static final int MAXENERGY = 100000;
    public static final int RECEIVEPERTICK = 500;

    public MatterReceiverTileEntity() {
        super(MAXENERGY, RECEIVEPERTICK);
    }

}
