package com.mcjty.rftools.blocks;

import com.mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class MachineBase extends Block {
    public MachineBase(Material material) {
        super(material);
        setBlockName("machineBase");
        setBlockTextureName(RFTools.MODID + ":" + "machineSide");
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return false;
    }
}
