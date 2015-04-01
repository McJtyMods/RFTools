package mcjty.rftools.apideps;

import mekanism.api.energy.IStrictEnergyStorage;
import net.minecraft.tileentity.TileEntity;

public class MekanismCompatibility {
    public static boolean isPowerStorage(TileEntity tileEntity) {
        return tileEntity instanceof IStrictEnergyStorage;
    }

    public static long getEnergyLevel(TileEntity tileEntity) {
        return (long) ((IStrictEnergyStorage) tileEntity).getEnergy();
    }

    public static long getMaxEnergyLevel(TileEntity tileEntity) {
        return (long) ((IStrictEnergyStorage) tileEntity).getMaxEnergy();
    }
}
