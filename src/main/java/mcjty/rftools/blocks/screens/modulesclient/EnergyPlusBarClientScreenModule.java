package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.lib.varia.Coordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EnergyPlusBarClientScreenModule extends EnergyBarClientScreenModule {

    @Override
    protected void setupCoordinateFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        coordinate = Coordinate.INVALID;
        if (tagCompound.hasKey("monitorx")) {
            this.dim = tagCompound.getInteger("dim");
            coordinate = new Coordinate(tagCompound.getInteger("monitorx"), tagCompound.getInteger("monitory"), tagCompound.getInteger("monitorz"));
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }
}
