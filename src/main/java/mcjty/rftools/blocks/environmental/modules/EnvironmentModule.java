package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.blocks.environmental.EnvironmentalControllerTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface EnvironmentModule {
    float getRfPerTick();

    void tick(World world, BlockPos pos, int radius, int miny, int maxy, EnvironmentalControllerTileEntity controllerTileEntity);

    void activate(boolean a);
}
