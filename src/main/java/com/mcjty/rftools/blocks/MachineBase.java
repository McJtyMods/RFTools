package com.mcjty.rftools.blocks;

import com.mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class MachineBase extends Block {
    public MachineBase() {
        super(Material.iron);
        setBlockName("machineBase");
        setBlockTextureName(RFTools.MODID + ":" + "machineSide");
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.3F, 1.0F);
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return false;
    }
}
