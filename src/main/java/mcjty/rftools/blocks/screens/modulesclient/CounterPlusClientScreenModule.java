package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.lib.varia.BlockPosTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class CounterPlusClientScreenModule extends CounterClientScreenModule {

    @Override
    protected void setupCoordinateFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        coordinate = BlockPosTools.INVALID;
        if (tagCompound.hasKey("monitorx")) {
            this.dim = tagCompound.getInteger("dim");
            coordinate = new BlockPos(tagCompound.getInteger("monitorx"), tagCompound.getInteger("monitory"), tagCompound.getInteger("monitorz"));
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }
}
