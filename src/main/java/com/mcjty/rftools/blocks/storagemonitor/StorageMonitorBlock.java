package com.mcjty.rftools.blocks.storagemonitor;

import com.mcjty.container.GenericContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class StorageMonitorBlock extends GenericContainerBlock {

    public StorageMonitorBlock(Material material) {
        super(material);
        setBlockName("storageMonitorBlock");
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new StorageMonitorTileEntity();
    }

    @Override
    public String getFrontIconName() {
        return "machineStorageMonitor";
    }
}
