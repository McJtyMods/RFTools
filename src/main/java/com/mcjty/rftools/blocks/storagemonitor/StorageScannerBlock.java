package com.mcjty.rftools.blocks.storagemonitor;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;

public class StorageScannerBlock extends GenericContainerBlock {

    public StorageScannerBlock(Material material) {
        super(material, StorageScannerTileEntity.class);
        setBlockName("storageScannerBlock");
    }

    @Override
    public String getFrontIconName() {
        return "machineStorageScanner";
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_STORAGE_SCANNER;
    }
}
