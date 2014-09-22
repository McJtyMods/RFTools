package com.mcjty.rftools.blocks.storagemonitor;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;

public class StorageMonitorBlock extends GenericContainerBlock {

    public StorageMonitorBlock(Material material) {
        super(material, StorageMonitorTileEntity.class);
        setBlockName("storageMonitorBlock");
    }

    @Override
    public String getFrontIconName() {
        return "machineStorageMonitor";
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_STORAGE_MONITOR;
    }
}
