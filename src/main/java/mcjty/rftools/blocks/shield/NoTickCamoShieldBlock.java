package mcjty.rftools.blocks.shield;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class NoTickCamoShieldBlock extends CamoShieldBlock {

    public NoTickCamoShieldBlock(String registryName, String unlocName, boolean opaque) {
        super(registryName, unlocName, opaque);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new NoTickShieldSolidBlockTileEntity();
    }
}
