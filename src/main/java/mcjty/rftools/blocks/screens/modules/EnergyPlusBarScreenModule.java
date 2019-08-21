package mcjty.rftools.blocks.screens.modules;

import mcjty.lib.varia.BlockPosTools;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EnergyPlusBarScreenModule extends EnergyBarScreenModule {

    @Override
    public void setupFromNBT(CompoundNBT tagCompound, int dim, BlockPos pos) {
        if (tagCompound != null) {
            helper.setShowdiff(tagCompound.getBoolean("showdiff"));
            coordinate = BlockPosTools.INVALID;
            if (tagCompound.hasKey("monitorx")) {
                if (tagCompound.hasKey("monitordim")) {
                    this.dim = tagCompound.getInt("monitordim");
                } else {
                    // Compatibility reasons
                    this.dim = tagCompound.getInt("dim");
                }
                coordinate = new BlockPos(tagCompound.getInt("monitorx"), tagCompound.getInt("monitory"), tagCompound.getInt("monitorz"));
                if(tagCompound.hasKey("monitorside")) {
                    side = Direction.VALUES[tagCompound.getInt("monitorside")];
                }
            }
        }
    }

    @Override
    public int getRfPerTick() {
        return ScreenConfiguration.ENERGYPLUS_RFPERTICK.get();
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked, PlayerEntity player) {

    }
}
