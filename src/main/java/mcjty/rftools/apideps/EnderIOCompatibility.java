package mcjty.rftools.apideps;

import crazypants.enderio.power.IPowerStorage;
import net.minecraft.tileentity.TileEntity;

public class EnderIOCompatibility {
    public static boolean isPowerStorage(TileEntity tileEntity) {
        return tileEntity instanceof IPowerStorage;
    }

    public static long getEnergyLevel(TileEntity tileEntity) {
        return ((IPowerStorage) tileEntity).getEnergyStoredL();
    }

    public static long getMaxEnergyLevel(TileEntity tileEntity) {
        return ((IPowerStorage) tileEntity).getMaxEnergyStoredL();
    }
}
