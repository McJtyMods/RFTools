package mcjty.rftools.network;

public class MachineInfo {
    private final int energy;
    private final int maxEnergy;
    private final Integer energyPerTick;

    public MachineInfo(int energy, int maxEnergy, Integer energyPerTick) {
        this.energy = energy;
        this.maxEnergy = maxEnergy;
        this.energyPerTick = energyPerTick;
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public Integer getEnergyPerTick() {
        return energyPerTick;
    }
}
