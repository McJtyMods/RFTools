package mcjty.rftools.blocks.screens.modules;

import mcjty.varia.Coordinate;
import net.minecraft.nbt.NBTTagCompound;

public class CounterPlusScreenModule extends CounterScreenModule {
    public static final int RFPERTICK = 30;

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        if (tagCompound != null) {
            coordinate = Coordinate.INVALID;
            if (tagCompound.hasKey("monitorx")) {
                this.dim = tagCompound.getInteger("dim");
                coordinate = new Coordinate(tagCompound.getInteger("monitorx"), tagCompound.getInteger("monitory"), tagCompound.getInteger("monitorz"));
            }
        }
    }

    @Override
    public int getRfPerTick() {
        return RFPERTICK;
    }

    @Override
    public void activate(int x, int y) {

    }
}
