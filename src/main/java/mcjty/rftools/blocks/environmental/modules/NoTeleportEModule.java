package mcjty.rftools.blocks.environmental.modules;

import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import mcjty.rftools.blocks.environmental.EnvironmentalControllerTileEntity;
import mcjty.rftools.blocks.environmental.NoTeleportAreaManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NoTeleportEModule extends BuffEModule {

    public NoTeleportEModule() {
        super(PlayerBuff.BUFF_NOTELEPORT);
    }

    @Override
    public float getRfPerTick() {
        return (float) (double) EnvironmentalConfiguration.NOTELEPORT_RFPERTICK.get();
    }

    @Override
    public void tick(World world, BlockPos pos, int radius, int miny, int maxy, EnvironmentalControllerTileEntity controllerTileEntity) {
        if (!isActive()) {
            return;
        }

        super.tick(world, pos, radius, miny, maxy, controllerTileEntity);

        NoTeleportAreaManager.markArea(new GlobalCoordinate(pos, world.getDimension().getType().getId()), radius, miny, maxy);
    }
}
