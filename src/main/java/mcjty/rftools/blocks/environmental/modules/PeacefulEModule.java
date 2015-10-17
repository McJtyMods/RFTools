package mcjty.rftools.blocks.environmental.modules;

import mcjty.lib.varia.Coordinate;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import mcjty.rftools.blocks.environmental.EnvironmentalControllerTileEntity;
import mcjty.rftools.blocks.environmental.PeacefulAreaManager;
import net.minecraft.world.World;

public class PeacefulEModule extends BuffEModule {

    public PeacefulEModule() {
        super(PlayerBuff.BUFF_PEACEFUL);
    }

    @Override
    public float getRfPerTick() {
        return EnvironmentalConfiguration.REGENERATION_RFPERTICK;
    }

    @Override
    public void tick(World world, int x, int y, int z, int radius, int miny, int maxy, EnvironmentalControllerTileEntity controllerTileEntity) {
        if (!isActive()) {
            return;
        }

        super.tick(world, x, y, z, radius, miny, maxy, controllerTileEntity);
        PeacefulAreaManager.markArea(new GlobalCoordinate(new Coordinate(x, y, z), world.provider.dimensionId), radius, miny, maxy);
    }
}
