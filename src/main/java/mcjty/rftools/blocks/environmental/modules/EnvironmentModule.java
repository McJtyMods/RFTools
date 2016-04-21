package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.blocks.environmental.EnvironmentalControllerTileEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface EnvironmentModule {
    float getRfPerTick();

    void tick(World world, BlockPos pos, int radius, int miny, int maxy, EnvironmentalControllerTileEntity controllerTileEntity);

    // Apply the effect once on an entity. Return true if it worked
    boolean apply(World world, BlockPos pos, EntityLivingBase entity, int duration);

    void activate(boolean a);
}
