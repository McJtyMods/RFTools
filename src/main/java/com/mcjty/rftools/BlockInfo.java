package com.mcjty.rftools;

import cofh.api.energy.IEnergyHandler;
import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

public class BlockInfo {
    private boolean first;
    private Coordinate coordinate;
    int energyStored;
    int maxEnergyStored;

    public BlockInfo(TileEntity tileEntity, Coordinate coordinate, boolean first) {
        this.first = first;
        this.coordinate = coordinate;
        fetchEnergyValues(tileEntity);
    }

    public BlockInfo(boolean first, Coordinate coordinate, int energyStored, int maxEnergyStored) {
        this.first = first;
        this.coordinate = coordinate;
        this.energyStored = energyStored;
        this.maxEnergyStored = maxEnergyStored;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public boolean isFirst() {
        return first;
    }

    public static String getReadableName(Block block, Coordinate coordinate, int metadata, WorldClient world) {
        List<ItemStack> itemStacks = block.getDrops(world, coordinate.getX(), coordinate.getY(), coordinate.getZ(), metadata, 1);
        Object descriptiveObject = block;
        if (itemStacks != null && !itemStacks.isEmpty()) {
            descriptiveObject = itemStacks.get(0).getItem();
            System.out.println("itemStacks.get(0).getDisplayName() = " + itemStacks.get(0).getDisplayName());
        }

        String displayName = getReadableName(descriptiveObject, metadata);
        return displayName;
    }

    public static String getReadableName(Object object, int metadata) {
        if (object instanceof Block) {
            return getReadableName((Block) object, metadata);
        } else if (object instanceof Item) {
            return getReadableName((Item) object, metadata);
        } else if (object instanceof ItemStack) {
            ItemStack s = (ItemStack) object;
            return s.getDisplayName();
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

    private void fetchEnergyValues(TileEntity tileEntity) {
        try {
            IEnergyHandler handler = (IEnergyHandler) tileEntity;
            maxEnergyStored = handler.getMaxEnergyStored(ForgeDirection.DOWN);
            energyStored = handler.getEnergyStored(ForgeDirection.DOWN);
        } catch (ClassCastException e) {
            // Not an energy handler. Just ignore
            maxEnergyStored = 0;
            energyStored = 0;
        }
    }

    public int getEnergyStored() {
        return energyStored;
    }

    public int getMaxEnergyStored() {
        return maxEnergyStored;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlockInfo blockInfo = (BlockInfo) o;

        if (energyStored != blockInfo.energyStored) return false;
        if (first != blockInfo.first) return false;
        if (maxEnergyStored != blockInfo.maxEnergyStored) return false;
        if (coordinate != null ? !coordinate.equals(blockInfo.coordinate) : blockInfo.coordinate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (first ? 1 : 0);
        result = 31 * result + (coordinate != null ? coordinate.hashCode() : 0);
        result = 31 * result + energyStored;
        result = 31 * result + maxEnergyStored;
        return result;
    }
}
