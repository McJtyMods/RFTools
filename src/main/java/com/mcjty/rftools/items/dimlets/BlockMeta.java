package com.mcjty.rftools.items.dimlets;

import net.minecraft.block.Block;

public class BlockMeta {
    private final Block block;
    private final byte meta;

    public BlockMeta(Block block, byte meta) {
        this.block = block;
        this.meta = meta;
    }

    public BlockMeta(Block block, int meta) {
        this.block = block;
        this.meta = (byte)meta;
    }

    public Block getBlock() {
        return block;
    }

    public byte getMeta() {
        return meta;
    }
}
