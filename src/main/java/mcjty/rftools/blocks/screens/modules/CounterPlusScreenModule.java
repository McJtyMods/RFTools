package mcjty.rftools.blocks.screens.modules;

import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.varia.Coordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class CounterPlusScreenModule extends CounterScreenModule {

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
        return ScreenConfiguration.COUNTERPLUS_RFPERTICK;
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }
}
