package mcjty.rftools.blocks.environmental.modules;

import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import mcjty.rftools.blocks.environmental.EnvironmentalControllerTileEntity;
import mcjty.rftools.blocks.environmental.PeacefulAreaManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PeacefulEModule extends BuffEModule {

    public PeacefulEModule() {
        super(PlayerBuff.BUFF_PEACEFUL);
    }

    @Override
    public float getRfPerTick() {
        return (float) EnvironmentalConfiguration.REGENERATION_RFPERTICK.get();
    }

    @Override
    public void tick(World world, BlockPos pos, int radius, int miny, int maxy, EnvironmentalControllerTileEntity controllerTileEntity) {
        if (!isActive()) {
            return;
        }

        super.tick(world, pos, radius, miny, maxy, controllerTileEntity);
        PeacefulAreaManager.markArea(new GlobalCoordinate(pos, world.provider.getDimension()), radius, miny, maxy);
    }
}
