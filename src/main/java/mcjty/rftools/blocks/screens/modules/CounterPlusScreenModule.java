package mcjty.rftools.blocks.screens.modules;

import mcjty.lib.varia.BlockPosTools;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CounterPlusScreenModule extends CounterScreenModule {

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, BlockPos pos) {
        if (tagCompound != null) {
            coordinate = BlockPosTools.INVALID;
            if (tagCompound.hasKey("monitorx")) {
                if (tagCompound.hasKey("monitordim")) {
                    this.dim = tagCompound.getInteger("monitordim");
                } else {
                    // Compatibility reasons
                    this.dim = tagCompound.getInteger("dim");
                }
                coordinate = new BlockPos(tagCompound.getInteger("monitorx"), tagCompound.getInteger("monitory"), tagCompound.getInteger("monitorz"));
            }
        }
    }

    @Override
    public int getRfPerTick() {
        return ScreenConfiguration.COUNTERPLUS_RFPERTICK.get();
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked, EntityPlayer player) {

    }
}
