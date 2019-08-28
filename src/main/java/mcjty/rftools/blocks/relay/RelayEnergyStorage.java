package mcjty.rftools.blocks.relay;

import mcjty.lib.varia.EnergyTools;
import net.minecraft.util.Direction;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

//@Optional.Interface(iface = "net.darkhax.tesla.api.ITeslaConsumer", modid = "tesla")
class RelayEnergyStorage implements IEnergyStorage {
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
        // @todo 1.14 optimal?
        return relayTileEntity.getCapability(CapabilityEnergy.ENERGY).map(h ->
            EnergyTools.getIntEnergyStored(h.getEnergyStored(), h.getMaxEnergyStored())).orElse(0);
    }

    @Override
    public int getMaxEnergyStored() {
        return relayTileEntity.getCapability(CapabilityEnergy.ENERGY).map(h ->
                h.getMaxEnergyStored()).orElse(0);
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
