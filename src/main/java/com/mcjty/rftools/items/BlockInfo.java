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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlockInfo blockInfo = (BlockInfo) o;

        if (first != blockInfo.first) return false;
        if (block != null ? !block.equals(blockInfo.block) : blockInfo.block != null) return false;
        if (tileEntity != null ? !tileEntity.equals(blockInfo.tileEntity) : blockInfo.tileEntity != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = tileEntity != null ? tileEntity.hashCode() : 0;
        result = 31 * result + (block != null ? block.hashCode() : 0);
        result = 31 * result + (first ? 1 : 0);
        return result;
    }
}
