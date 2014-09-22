package com.mcjty.rftools.blocks.storagemonitor;

import com.mcjty.container.GenericEnergyHandlerContainer;
import net.minecraft.entity.player.EntityPlayer;

public class StorageMonitorContainer extends GenericEnergyHandlerContainer {

    public StorageMonitorContainer(EntityPlayer player, StorageMonitorTileEntity storageMonitorTileEntity) {
        super(StorageMonitorContainerFactory.getInstance(), player, storageMonitorTileEntity);
    }
}
