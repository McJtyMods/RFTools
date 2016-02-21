package mcjty.rftools.api.screens;

import mcjty.rftools.api.screens.data.IModuleData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public interface IScreenModule<T extends IModuleData> {
    /**
     * Get the data that can be used client side to help render this module.
     * If you don't need data from the server side you can return null here.
     */
    T getData(IScreenDataHelper helper, World worldObj, long millis);

    void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z);

    int getRfPerTick();

    void mouseClick(World world, int x, int y, boolean clicked);
}
