package mcjty.rftools.blocks.relay;

import mcjty.lib.varia.EnergyTools;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraft.util.Direction;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nullable;

@Optional.Interface(iface = "net.darkhax.tesla.api.ITeslaConsumer", modid = "tesla")
class RelayEnergyStorage implements IEnergyStorage, ITeslaConsumer {
    private final RelayTileEntity relayTileEntity;
    private final Direction side;

    public RelayEnergyStorage(RelayTileEntity relayTileEntity, @Nullable Direction side) {
        this.relayTileEntity = relayTileEntity;
        this.side = side;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return relayTileEntity.receiveEnergy(side, maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return EnergyTools.getIntEnergyStored(relayTileEntity.getStoredPower(), relayTileEntity.getCapacity());
    }

    @Override
    public int getMaxEnergyStored() {
        return EnergyTools.unsignedClampToInt(relayTileEntity.getCapacity());
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    @Optional.Method(modid = "tesla")
    @Override
    public long givePower(long power, boolean simulated) {
        return relayTileEntity.receiveEnergy(side, EnergyTools.unsignedClampToInt(power), simulated);
    }
}
