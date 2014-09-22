package com.mcjty.rftools.blocks;

import com.mcjty.rftools.blocks.crafter.CrafterBlock;
import com.mcjty.rftools.blocks.crafter.CrafterBlockTileEntity;
import com.mcjty.rftools.blocks.monitor.RFMonitorBlock;
import com.mcjty.rftools.blocks.monitor.RFMonitorBlockTileEntity;
import com.mcjty.rftools.blocks.storagemonitor.StorageMonitorBlock;
import com.mcjty.rftools.blocks.storagemonitor.StorageMonitorTileEntity;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public final class ModBlocks {
    public static RFMonitorBlock monitorBlock;
    public static CrafterBlock crafterBlock;
    public static StorageMonitorBlock storageMonitorBlock;
    public static MachineFrame machineFrame;

    public static final void init() {
        monitorBlock = new RFMonitorBlock(Material.iron);
        monitorBlock.setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerBlock(monitorBlock, "rfMonitorBlock");
        GameRegistry.registerTileEntity(RFMonitorBlockTileEntity.class, "RFMonitorTileEntity");

        crafterBlock = new CrafterBlock(Material.iron);
        crafterBlock.setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerBlock(crafterBlock, "crafterBlock");
        GameRegistry.registerTileEntity(CrafterBlockTileEntity.class, "CrafterTileEntity");

        storageMonitorBlock = new StorageMonitorBlock(Material.iron);
        storageMonitorBlock.setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerBlock(storageMonitorBlock, "storageMonitorBlock");
        GameRegistry.registerTileEntity(StorageMonitorTileEntity.class, "StorageMonitorTileEntity");

        machineFrame = new MachineFrame(Material.iron);
        machineFrame.setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerBlock(machineFrame, "machineFrame");
    }
}
