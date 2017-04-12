package mcjty.rftools.blocks.elevator;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Set;

public class FakeElevatorWorld implements IBlockAccess {

    private Set<BlockPos> positions;
    private IBlockState state;
    private final IBlockState AIR = Blocks.AIR.getDefaultState();


    public void setState(ElevatorTileEntity elevatorTileEntity, IBlockState state) {
        this.state = state;
        positions = elevatorTileEntity.getPositions();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return 0xf000f0;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }


    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return positions.contains(pos) ? state : AIR;
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return !positions.contains(pos);
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return Biomes.PLAINS;
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return 0;
    }

    @Override
    public WorldType getWorldType() {
        return WorldType.DEFAULT;
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        return getBlockState(pos).isSideSolid(this, pos, side);
    }
}
