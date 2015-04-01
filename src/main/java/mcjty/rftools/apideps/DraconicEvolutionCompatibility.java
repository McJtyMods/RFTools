package mcjty.rftools.apideps;

import brandon3055.draconicevolution.api.IExtendedRFStorage;
import net.minecraft.tileentity.TileEntity;

public class DraconicEvolutionCompatibility {
    public static boolean isPowerStorage(TileEntity tileEntity) {
        return tileEntity instanceof IExtendedRFStorage;
    }

    public static long getEnergyLevel(TileEntity tileEntity) {
        return (long) ((IExtendedRFStorage) tileEntity).getEnergyStored();
    }

    public static long getMaxEnergyLevel(TileEntity tileEntity) {
        return (long) ((IExtendedRFStorage) tileEntity).getMaxEnergyStored();
    }
}
