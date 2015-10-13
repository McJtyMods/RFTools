package mcjty.rftools.blocks.shield;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class NoTickInvisibleShieldBlock extends InvisibleShieldBlock {
    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new NoTickShieldBlockTileEntity();
    }
}
