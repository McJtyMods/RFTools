package com.mcjty.rftools.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.material.Material;

public final class ModBlocks {
    public static RFMonitorBlock monitorBlock;

    public static final void init() {
        monitorBlock = new RFMonitorBlock(Material.iron);
        GameRegistry.registerBlock(monitorBlock, "rfMonitorBlock");
    }
}
