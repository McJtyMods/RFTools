package mcjty.rftools.network;

public class MachineInfo {
    private final long energy;
    private final long maxEnergy;
    private final Long energyPerTick;

    public MachineInfo(long energy, long maxEnergy, Long energyPerTick) {
        this.energy = energy;
        this.maxEnergy = maxEnergy;
        this.energyPerTick = energyPerTick;
    }

    public long getEnergy() {
        return energy;
    }

    public long getMaxEnergy() {
        return maxEnergy;
    }

    public Long getEnergyPerTick() {
        return energyPerTick;
    }
}
