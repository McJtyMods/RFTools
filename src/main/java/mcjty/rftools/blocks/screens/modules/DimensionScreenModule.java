package mcjty.rftools.blocks.screens.modules;

import mcjty.rftools.blocks.dimlets.DimletConfiguration;
import mcjty.rftools.dimension.DimensionStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.DimensionManager;

public class DimensionScreenModule implements ScreenModule {
    public static final int RFPERTICK = 6;
    private int dim = 0;
    private ScreenModuleHelper helper = new ScreenModuleHelper();

    @Override
    public Object[] getData(long millis) {
        int energy = DimensionStorage.getDimensionStorage(DimensionManager.getWorld(0)).getEnergyLevel(dim);
        return helper.getContentsValue(millis, energy, DimletConfiguration.MAX_DIMENSION_POWER);
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        if (tagCompound != null) {
            this.dim = tagCompound.getInteger("dim");
            helper.setShowdiff(tagCompound.getBoolean("showdiff"));
        }
    }

    @Override
    public int getRfPerTick() {
        return RFPERTICK;
    }
}
