package com.mcjty.rftools.blocks.storagemonitor;

import com.mcjty.container.ContainerFactory;

public class StorageScannerContainerFactory extends ContainerFactory {

    private static StorageScannerContainerFactory instance = null;

    public static synchronized StorageScannerContainerFactory getInstance() {
        if (instance == null) {
            instance = new StorageScannerContainerFactory();
        }
        return instance;
    }


}
