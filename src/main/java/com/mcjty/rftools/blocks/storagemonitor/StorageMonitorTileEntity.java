package com.mcjty.rftools.blocks.storagemonitor;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;

public class StorageMonitorTileEntity extends GenericEnergyHandlerTileEntity {
    public static final int MAXENERGY = 100000;
    public static final int RECEIVEPERTICK = 100;


    public StorageMonitorTileEntity() {
        super(MAXENERGY, RECEIVEPERTICK);
    }

    public void startScan(int radius) {

    }
}
