package mcjty.rftools.apideps;

import mekanism.api.energy.IStrictEnergyStorage;
import net.minecraft.tileentity.TileEntity;

public class MekanismCompatibility {
    private static double MEK_TO_RF = 1.0 / 2.5;

    public static boolean isPowerStorage(TileEntity tileEntity) {
        return tileEntity instanceof IStrictEnergyStorage;
    }

    public static long getEnergyLevel(TileEntity tileEntity) {
        return (long) (((IStrictEnergyStorage) tileEntity).getEnergy() * MEK_TO_RF);
    }

    public static long getMaxEnergyLevel(TileEntity tileEntity) {
        return (long) (((IStrictEnergyStorage) tileEntity).getMaxEnergy() * MEK_TO_RF);
    }
}
