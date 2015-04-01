package mcjty.rftools.blocks.environmental.modules;

import net.minecraft.world.World;

public interface EnvironmentModule {
    float getRfPerTick();

    void tick(World world, int x, int y, int z, int radius, int miny, int maxy);

    void activate(boolean a);
}
