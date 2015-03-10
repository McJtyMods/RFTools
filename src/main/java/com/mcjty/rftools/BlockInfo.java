package com.mcjty.rftools;

import com.mcjty.varia.Coordinate;
import com.mcjty.varia.EnergyTools;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.List;

public class BlockInfo {
    private Coordinate coordinate;
    private int energyStored;
    private int maxEnergyStored;

    public BlockInfo(TileEntity tileEntity, Coordinate coordinate) {
        this.coordinate = coordinate;
        fetchEnergyValues(tileEntity);
    }

    public BlockInfo(Coordinate coordinate, int energyStored, int maxEnergyStored) {
        this.coordinate = coordinate;
        this.energyStored = energyStored;
        this.maxEnergyStored = maxEnergyStored;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public static String getReadableName(Block block, Coordinate coordinate, int metadata, World world) {
        List<ItemStack> itemStacks = block.getDrops(world, coordinate.getX(), coordinate.getY(), coordinate.getZ(), metadata, 1);
        if (itemStacks != null && !itemStacks.isEmpty() && itemStacks.get(0).getItem() != null) {
            return getReadableName(itemStacks.get(0).getItem(), metadata);
        }

        return getReadableName(block, metadata);
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

    public static String getReadableName(Block block, int metadata) {
        ItemStack s = new ItemStack(block, 1, metadata);
        String displayName;
        if (s.getItem() == null) {
            return block.getUnlocalizedName();
        } else {
            displayName = s.getDisplayName();
        }
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
        EnergyTools.EnergyLevel energyLevel = EnergyTools.getEnergyLevel(tileEntity);
        maxEnergyStored = energyLevel.getMaxEnergy();
        energyStored = energyLevel.getEnergy();
    }

    public int getEnergyStored() {
        return energyStored;
    }

    public int getMaxEnergyStored() {
        return maxEnergyStored;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BlockInfo blockInfo = (BlockInfo) o;

        if (energyStored != blockInfo.energyStored) {
            return false;
        }
        if (maxEnergyStored != blockInfo.maxEnergyStored) {
            return false;
        }
        if (coordinate != null ? !coordinate.equals(blockInfo.coordinate) : blockInfo.coordinate != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = coordinate != null ? coordinate.hashCode() : 0;
        result = 31 * result + energyStored;
        result = 31 * result + maxEnergyStored;
        return result;
    }
}
