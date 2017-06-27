package mcjty.rftools.blocks.relay;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

class RelayEnergyStorage implements IEnergyStorage {
    private final RelayTileEntity relayTileEntity;
    private final EnumFacing side;

    public RelayEnergyStorage(RelayTileEntity relayTileEntity, @Nullable EnumFacing side) {
        this.relayTileEntity = relayTileEntity;
        this.side = side;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return relayTileEntity.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return relayTileEntity.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return relayTileEntity.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}
