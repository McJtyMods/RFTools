package com.mcjty.rftools.blocks.endergen;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;

public class PearlInjectorBlock extends GenericContainerBlock {

    public PearlInjectorBlock(Material material) {
        super(material, PearlInjectorTileEntity.class);
        setBlockName("pearlInjectorBlock");
    }

    @Override
    public String getFrontIconName() {
        return "machinePearlInjector";
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_PEARL_INJECTOR;
    }
}
