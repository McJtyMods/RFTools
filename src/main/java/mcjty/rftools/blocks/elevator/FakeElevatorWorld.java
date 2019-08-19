package mcjty.rftools.blocks.elevator;

import net.minecraft.block.state.BlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Set;

public class FakeElevatorWorld implements IBlockAccess {

    private Set<BlockPos> positions;
    private BlockState state;
    private World realWorld;
    private final BlockState AIR = Blocks.AIR.getDefaultState();


    public void setWorldAndState(ElevatorTileEntity elevatorTileEntity) {
        this.state = elevatorTileEntity.getMovingState();
        this.realWorld = elevatorTileEntity.getWorld();
        positions = elevatorTileEntity.getPositions();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return realWorld.getCombinedLight(pos, lightValue);
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
    public boolean isAirBlock(BlockPos pos) {
        return !positions.contains(pos);
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return realWorld.getBiome(pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, Direction direction) {
        return 0;
    }

    @Override
    public WorldType getWorldType() {
        return realWorld.getWorldType();
    }

    @Override
    public boolean isSideSolid(BlockPos pos, Direction side, boolean _default) {
        return getBlockState(pos).isSideSolid(this, pos, side);
    }
}
