package mcjty.rftools.blocks.screens.modules;

import mcjty.lib.varia.Coordinate;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemStackPlusScreenModule extends ItemStackScreenModule {

    @Override
    protected void setupCoordinateFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        coordinate = Coordinate.INVALID;
        if (tagCompound.hasKey("monitorx")) {
            this.dim = tagCompound.getInteger("dim");
            coordinate = new Coordinate(tagCompound.getInteger("monitorx"), tagCompound.getInteger("monitory"), tagCompound.getInteger("monitorz"));
        }
    }

    @Override
    public int getRfPerTick() {
        return ScreenConfiguration.ITEMSTACKPLUS_RFPERTICK;
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }
}
