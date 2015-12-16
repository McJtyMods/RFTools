package mcjty.rftools.varia;

import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class EnergyTools {

    public static class EnergyLevel {
        private final int energy;
        private final int maxEnergy;

        public EnergyLevel(int energy, int maxEnergy) {
            this.energy = energy;
            this.maxEnergy = maxEnergy;
        }

        public int getEnergy() {
            return energy;
        }

        public int getMaxEnergy() {
            return maxEnergy;
        }
    }

    public static class EnergyLevelMulti {
        private final long energy;
        private final long maxEnergy;

        public EnergyLevelMulti(long energy, long maxEnergy) {
            this.energy = energy;
            this.maxEnergy = maxEnergy;
        }

        public long getEnergy() {
            return energy;
        }

        public long getMaxEnergy() {
            return maxEnergy;
        }
    }

    public static boolean isEnergyTE(TileEntity te) {
        return te instanceof IEnergyHandler || te instanceof IEnergyReceiver || te instanceof IEnergyProvider;
    }

    // Get energy level with possible support for multiblocks (like EnderIO capacitor bank).
    public static EnergyLevelMulti getEnergyLevelMulti(TileEntity tileEntity) {
        long maxEnergyStored;
        long energyStored;
        if (tileEntity instanceof IEnergyHandler) {
            IEnergyHandler handler = (IEnergyHandler) tileEntity;
            maxEnergyStored = handler.getMaxEnergyStored(EnumFacing.DOWN);
            energyStored = handler.getEnergyStored(EnumFacing.DOWN);
        } else if (tileEntity instanceof IEnergyReceiver) {
            IEnergyReceiver handler = (IEnergyReceiver) tileEntity;
            maxEnergyStored = handler.getMaxEnergyStored(EnumFacing.DOWN);
            energyStored = handler.getEnergyStored(EnumFacing.DOWN);
        } else if (tileEntity instanceof IEnergyProvider) {
            IEnergyProvider handler = (IEnergyProvider) tileEntity;
            maxEnergyStored = handler.getMaxEnergyStored(EnumFacing.DOWN);
            energyStored = handler.getEnergyStored(EnumFacing.DOWN);
        } else {
            maxEnergyStored = 0;
            energyStored = 0;
        }
        return new EnergyLevelMulti(energyStored, maxEnergyStored);
    }

    public static EnergyLevel getEnergyLevel(TileEntity tileEntity) {
        int maxEnergyStored;
        int energyStored;
        if (tileEntity instanceof IEnergyHandler) {
            IEnergyHandler handler = (IEnergyHandler) tileEntity;
            maxEnergyStored = handler.getMaxEnergyStored(EnumFacing.DOWN);
            energyStored = handler.getEnergyStored(EnumFacing.DOWN);
        } else if (tileEntity instanceof IEnergyReceiver) {
            IEnergyReceiver handler = (IEnergyReceiver) tileEntity;
            maxEnergyStored = handler.getMaxEnergyStored(EnumFacing.DOWN);
            energyStored = handler.getEnergyStored(EnumFacing.DOWN);
        } else if (tileEntity instanceof IEnergyProvider) {
            IEnergyProvider handler = (IEnergyProvider) tileEntity;
            maxEnergyStored = handler.getMaxEnergyStored(EnumFacing.DOWN);
            energyStored = handler.getEnergyStored(EnumFacing.DOWN);
        } else {
            maxEnergyStored = 0;
            energyStored = 0;
        }
        return new EnergyLevel(energyStored, maxEnergyStored);
    }

    public static int extractEnergy(TileEntity tileEntity, EnumFacing from, int maxExtract) {
        if (tileEntity instanceof IEnergyHandler) {
            return ((IEnergyHandler) tileEntity).extractEnergy(from, maxExtract, false);
        } else if (tileEntity instanceof IEnergyProvider) {
            return ((IEnergyProvider) tileEntity).extractEnergy(from, maxExtract, false);
        } else {
            return 0;
        }
    }

    public static int receiveEnergy(TileEntity tileEntity, EnumFacing from, int maxReceive) {
        if (tileEntity instanceof IEnergyHandler) {
            return ((IEnergyHandler) tileEntity).receiveEnergy(from, maxReceive, false);
        } else if (tileEntity instanceof IEnergyReceiver) {
            return ((IEnergyReceiver) tileEntity).receiveEnergy(from, maxReceive, false);
        } else {
            return 0;
        }
    }
}
