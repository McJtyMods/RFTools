package mcjty.rftools.blocks.relay;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nullable;

import mcjty.lib.varia.EnergyTools;

@Optional.InterfaceList({
    @Optional.Interface(iface = "net.darkhax.tesla.api.ITeslaConsumer", modid = "tesla"),
    @Optional.Interface(iface = "net.darkhax.tesla.api.ITeslaHolder", modid = "tesla")
})
class RelayEnergyStorage implements IEnergyStorage, ITeslaConsumer, ITeslaHolder {
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

    @Optional.Method(modid = "tesla")
    @Override
    public long givePower(long power, boolean simulated) {
        return relayTileEntity.receiveEnergy(EnergyTools.unsignedClampToInt(power), simulated);
    }

    @Optional.Method(modid = "tesla")
    @Override
    public long getStoredPower() {
        return relayTileEntity.getEnergyStored();
    }

    @Optional.Method(modid = "tesla")
    @Override
    public long getCapacity() {
        return relayTileEntity.getMaxEnergyStored();
    }
}
