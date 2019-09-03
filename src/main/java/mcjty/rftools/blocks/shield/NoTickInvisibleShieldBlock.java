package mcjty.rftools.blocks.shield;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import static mcjty.rftools.blocks.shield.ShieldSetup.TYPE_SHIELD_INV_NO_TICK_BLOCK;

public class NoTickInvisibleShieldBlock extends InvisibleShieldBlock {

    public NoTickInvisibleShieldBlock(String registryName, String unlocName, boolean opaque) {
        super(registryName, unlocName, opaque);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new NoTickShieldBlockTileEntity(TYPE_SHIELD_INV_NO_TICK_BLOCK);
    }
}
