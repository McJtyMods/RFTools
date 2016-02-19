package mcjty.rftools.api.screens;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public interface IScreenModule {
    Object[] getData(World worldObj, long millis);

    void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z);

    int getRfPerTick();

    void mouseClick(World world, int x, int y, boolean clicked);
}
