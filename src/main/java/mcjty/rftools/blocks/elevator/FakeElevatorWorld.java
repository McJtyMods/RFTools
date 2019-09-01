package mcjty.rftools.blocks.elevator;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Set;

public class FakeElevatorWorld implements IBlockReader {

    private Set<BlockPos> positions;
    private BlockState state;
    private World realWorld;
    private final BlockState AIR = Blocks.AIR.getDefaultState();


    public void setWorldAndState(ElevatorTileEntity elevatorTileEntity) {
        this.state = elevatorTileEntity.getMovingState();
        this.realWorld = elevatorTileEntity.getWorld();
        positions = elevatorTileEntity.getPositions();
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }


    @Override
    public BlockState getBlockState(BlockPos pos) {
        return positions.contains(pos) ? state : AIR;
    }

    @Override
    public IFluidState getFluidState(BlockPos blockPos) {
        return null;
    }
}
