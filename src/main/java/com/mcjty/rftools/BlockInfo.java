package com.mcjty.rftools;

import cofh.api.energy.IEnergyHandler;
import net.minecraft.block.Block;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

public class BlockInfo {
    private TileEntity tileEntity;
    private Block block;
    private int metadata;
    private boolean first;

    public BlockInfo(TileEntity tileEntity, Block block, int metadata, boolean first) {
        this.tileEntity = tileEntity;
        this.block = block;
        this.metadata = metadata;
        this.first = first;
    }

    public TileEntity getTileEntity() {
        return tileEntity;
    }

    public Block getBlock() {
        return block;
    }

    public int getMetadata() {
        return metadata;
    }

    public boolean isFirst() {
        return first;
    }

    public String getReadableName(WorldClient world, Coordinate coordinate) {
        List<ItemStack> itemStacks = block.getDrops(world, coordinate.getX(), coordinate.getY(), coordinate.getZ(), metadata, 1);
        Object descriptiveObject = block;
        if (itemStacks != null && !itemStacks.isEmpty()) {
            descriptiveObject = itemStacks.get(0).getItem();
            System.out.println("itemStacks.get(0).getDisplayName() = " + itemStacks.get(0).getDisplayName());
        }

        String displayName = getReadableName(descriptiveObject, metadata);
        return displayName;
    }

    private static String getReadableName(Object object, int metadata) {
        if (object instanceof Block) {
            return getReadableName((Block) object, metadata);
        } else if (object instanceof Item) {
            return getReadableName((Item) object, metadata);
        } else {
            return "?";
        }
    }

    private static String getReadableName(Block block, int metadata) {
        ItemStack s = new ItemStack(block, 1, metadata);
        String displayName = s.getDisplayName();
        if (displayName.startsWith("tile.")) {
            displayName = displayName.substring(5);
        }
        if (displayName.endsWith(".name")) {
            displayName = displayName.substring(0, displayName.length()-5);
        }
        return displayName;
    }

    private static String getReadableName(Item item, int metadata) {
        ItemStack s = new ItemStack(item, 1, metadata);
        String displayName = s.getDisplayName();
        if (displayName.startsWith("tile.")) {
            displayName = displayName.substring(5);
        }
        if (displayName.endsWith(".name")) {
            displayName = displayName.substring(0, displayName.length()-5);
        }
        return displayName;
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
        if (metadata != blockInfo.metadata) return false;
        if (block != null ? !block.equals(blockInfo.block) : blockInfo.block != null) return false;
        if (tileEntity != null ? !tileEntity.equals(blockInfo.tileEntity) : blockInfo.tileEntity != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = tileEntity != null ? tileEntity.hashCode() : 0;
        result = 31 * result + (block != null ? block.hashCode() : 0);
        result = 31 * result + metadata;
        result = 31 * result + (first ? 1 : 0);
        return result;
    }
}
