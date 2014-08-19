package com.mcjty.rftools.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class RFMonitorBlock extends Block {

    public RFMonitorBlock(Material material) {
        super(material);
        setBlockName("rfMonitorBlock");
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sidex, float sidey, float sidez) {
        return super.onBlockActivated(world, x, y, z, player, side, sidex, sidey, sidez);
    }

}
