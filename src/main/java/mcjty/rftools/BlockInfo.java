package mcjty.rftools;

import javax.annotation.Nullable;

import mcjty.lib.varia.EnergyTools;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class BlockInfo {
    private BlockPos coordinate;
    private long storedPower;
    private long capacity;

    public BlockInfo(TileEntity tileEntity, @Nullable EnumFacing side, BlockPos coordinate) {
        this.coordinate = coordinate;
        fetchEnergyValues(tileEntity, side);
    }

    public BlockInfo(BlockPos coordinate, long storedPower, long capacity) {
        this.coordinate = coordinate;
        this.storedPower = storedPower;
        this.capacity = capacity;
    }

    public BlockPos getCoordinate() {
        return coordinate;
    }

    private void fetchEnergyValues(TileEntity tileEntity, @Nullable EnumFacing side) {
        EnergyTools.EnergyLevel energyLevel = EnergyTools.getEnergyLevel(tileEntity, side);
        capacity = energyLevel.getMaxEnergy();
        storedPower = energyLevel.getEnergy();
    }

    public long getStoredPower() {
        return storedPower;
    }

    public long getCapacity() {
        return capacity;
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

        if (storedPower != blockInfo.storedPower) {
            return false;
        }
        if (capacity != blockInfo.capacity) {
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
        result = 31 * result + Long.hashCode(storedPower);
        result = 31 * result + Long.hashCode(capacity);
        return result;
    }
}
