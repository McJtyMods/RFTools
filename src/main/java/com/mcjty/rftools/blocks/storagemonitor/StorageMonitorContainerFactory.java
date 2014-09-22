package com.mcjty.rftools.blocks.storagemonitor;

import com.mcjty.container.ContainerFactory;

public class StorageMonitorContainerFactory extends ContainerFactory {

    private static StorageMonitorContainerFactory instance = null;

    public static synchronized StorageMonitorContainerFactory getInstance() {
        if (instance == null) {
            instance = new StorageMonitorContainerFactory();
        }
        return instance;
    }


}
