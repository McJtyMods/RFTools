package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.lib.varia.BlockPosTools;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EnergyPlusBarClientScreenModule extends EnergyBarClientScreenModule {

    @Override
    protected void setupCoordinateFromNBT(CompoundNBT tagCompound, int dim, BlockPos pos) {
        coordinate = BlockPosTools.INVALID;
        if (tagCompound.contains("monitorx")) {
            if (tagCompound.contains("monitordim")) {
                this.dim = tagCompound.getInt("monitordim");
            } else {
                // Compatibility reasons
                this.dim = tagCompound.getInt("dim");
            }
            coordinate = new BlockPos(tagCompound.getInt("monitorx"), tagCompound.getInt("monitory"), tagCompound.getInt("monitorz"));
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }
}
