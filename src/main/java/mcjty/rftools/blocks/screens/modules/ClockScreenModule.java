package mcjty.rftools.blocks.screens.modules;

import mcjty.rftools.api.screens.IScreenDataHelper;
import mcjty.rftools.api.screens.IScreenModule;
import mcjty.rftools.api.screens.data.IModuleData;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ClockScreenModule implements IScreenModule<IModuleData> {

    @Override
    public IModuleData getData(IScreenDataHelper helper, World worldObj, long millis) {
        return null;
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, BlockPos pos) {

    }

    @Override
    public int getRfPerTick() {
        return ScreenConfiguration.CLOCK_RFPERTICK.get();
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked, EntityPlayer player) {

    }
}
