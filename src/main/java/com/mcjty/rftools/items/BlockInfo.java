package com.mcjty.rftools.items;

import cofh.api.energy.IEnergyHandler;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockInfo {
    TileEntity tileEntity;
    Block block;
    boolean first;

    BlockInfo(TileEntity tileEntity, Block block, boolean first) {
        this.tileEntity = tileEntity;
        this.block = block;
        this.first = first;
    }

    public TileEntity getTileEntity() {
        return tileEntity;
    }

    public Block getBlock() {
        return block;
    }

    public boolean isFirst() {
        return first;
    }

    public int getEnergyStored() {
        try {
            IEnergyHandler handler = (IEnergyHandler) tileEntity;
            return handler.getEnergyStored(ForgeDirection.DOWN);
        } catch (ClassCastException e) {
            // Not an energy handler. Just ignore
            return 0;
        }
    }

    public int getMaxEnergyStored() {
        try {
            IEnergyHandler handler = (IEnergyHandler) tileEntity;
            return handler.getMaxEnergyStored(ForgeDirection.DOWN);
        } catch (ClassCastException e) {
            // Not an energy handler. Just ignore
            return 0;
        }
    }
}
