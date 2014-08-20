package com.mcjty.rftools.blocks;

import com.mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class MachineFrame extends Block {
    public MachineFrame(Material material) {
        super(material);
        setBlockName("machineFrame");
        setBlockTextureName(RFTools.MODID + ":" + "machineSide");
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return false;
    }
}
