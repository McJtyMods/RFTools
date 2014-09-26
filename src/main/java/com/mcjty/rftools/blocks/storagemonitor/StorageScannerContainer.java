package com.mcjty.rftools.blocks.storagemonitor;

import com.mcjty.container.GenericEnergyHandlerContainer;
import net.minecraft.entity.player.EntityPlayer;

public class StorageScannerContainer extends GenericEnergyHandlerContainer {

    public StorageScannerContainer(EntityPlayer player, StorageScannerTileEntity storageScannerTileEntity) {
        super(StorageScannerContainerFactory.getInstance(), player, storageScannerTileEntity);
    }
}
