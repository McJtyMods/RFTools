package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.container.ContainerFactory;

public class MatterTransmitterContainerFactory  extends ContainerFactory {

    private static MatterTransmitterContainerFactory instance = null;

    public static synchronized MatterTransmitterContainerFactory getInstance() {
        if (instance == null) {
            instance = new MatterTransmitterContainerFactory();
        }
        return instance;
    }

}
