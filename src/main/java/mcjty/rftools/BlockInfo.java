package mcjty.rftools;

import mcjty.lib.varia.EnergyTools;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class BlockInfo {
    private BlockPos coordinate;
    private long energyStored;
    private long maxEnergyStored;

    public BlockInfo(TileEntity tileEntity, BlockPos coordinate) {
        this.coordinate = coordinate;
        fetchEnergyValues(tileEntity);
    }

    public BlockInfo(BlockPos coordinate, long energyStored, long maxEnergyStored) {
        this.coordinate = coordinate;
        this.energyStored = energyStored;
        this.maxEnergyStored = maxEnergyStored;
    }

    public BlockPos getCoordinate() {
        return coordinate;
    }

    private void fetchEnergyValues(TileEntity tileEntity) {
        EnergyTools.EnergyLevel energyLevel = EnergyTools.getEnergyLevel(tileEntity);
        maxEnergyStored = energyLevel.getMaxEnergy();
        energyStored = energyLevel.getEnergy();
    }

    public long getEnergyStored() {
        return energyStored;
    }

    public long getMaxEnergyStored() {
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
        result = 31 * result + Long.hashCode(energyStored);
        result = 31 * result + Long.hashCode(maxEnergyStored);
        return result;
    }
}
