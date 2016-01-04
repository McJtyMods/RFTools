package mcjty.rftools.blocks.screens.modules;

import mcjty.rftools.blocks.screens.ScreenConfiguration;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class TextScreenModule implements ScreenModule {

    @Override
    public Object[] getData(World worldObj, long millis) {
        return null;
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {

    }

    @Override
    public int getRfPerTick() {
        return ScreenConfiguration.TEXT_RFPERTICK;
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }
}
