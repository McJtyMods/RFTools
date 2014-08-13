package com.mcjty.rftools.items;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

public class BlockInfo {
    TileEntity tileEntity;
    Block block;

    BlockInfo(TileEntity tileEntity, Block block) {
        this.tileEntity = tileEntity;
        this.block = block;
    }

    public TileEntity getTileEntity() {
        return tileEntity;
    }

    public Block getBlock() {
        return block;
    }
}
