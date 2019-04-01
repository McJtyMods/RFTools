package mcjty.rftools.blocks.logic.wire;


import mcjty.lib.tileentity.LogicTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class WireTileEntity extends LogicTileEntity {

    private int loopDetector = 0;

    @Override
    public int getRedstoneOutput(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        if (side == getFacing(state).getInputSide()) {
            return powerLevel;
        } else {
            return 0;
        }
    }

    @Override
    public void checkRedstone(World world, BlockPos pos) {
        super.checkRedstone(world, pos);
        if (loopDetector <= 0) {
            loopDetector++;
            IBlockState state = world.getBlockState(pos);
            BlockPos offsetPos = pos.offset(getFacing(state).getInputSide().getOpposite());
            if (world.isBlockLoaded(offsetPos)) {
                world.neighborChanged(offsetPos, state.getBlock(), pos);
            }
            loopDetector--;
        }
    }
}
