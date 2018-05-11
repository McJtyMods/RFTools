package mcjty.rftools.blocks.shield;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class NoTickInvisibleShieldBlock extends InvisibleShieldBlock {

    public NoTickInvisibleShieldBlock(String registryName, String unlocName, boolean opaque) {
        super(registryName, unlocName, opaque);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new NoTickShieldBlockTileEntity();
    }
}
