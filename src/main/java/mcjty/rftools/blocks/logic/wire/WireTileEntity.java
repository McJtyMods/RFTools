package mcjty.rftools.blocks.logic.wire;


import mcjty.lib.tileentity.LogicTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class WireTileEntity extends LogicTileEntity {

    @Override
    public int getRedstoneOutput(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        if (side == getFacing(state).getInputSide()) {
            return powerLevel;
        } else {
            return 0;
        }
    }
}
