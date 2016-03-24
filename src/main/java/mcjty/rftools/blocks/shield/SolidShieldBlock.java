package mcjty.rftools.blocks.shield;

import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SolidShieldBlock extends AbstractShieldBlock {

    public static final PropertyInteger ICON_TOPDOWN = PropertyInteger.create("icontopdown", 0, 3);
    public static final PropertyInteger ICON_SIDE = PropertyInteger.create("iconside", 0, 3);

    @Override
    protected void init() {
        setRegistryName("solid_shield_block");
        setUnlocalizedName("solid_shield_block");
    }

    @Override
    protected void initTE() {
        GameRegistry.registerTileEntity(TickShieldBlockTileEntity.class, RFTools.MODID + "_" + getRegistryName());
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TickShieldBlockTileEntity();
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        Block block = world.getBlockState(pos.offset(side)).getBlock();
        if (block == ShieldSetup.solidShieldBlock || block == ShieldSetup.noTickSolidShieldBlock) {
            return false;
        }
        return true;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int topdown = (z & 0x1) * 2 + (x & 0x1);
        int side = (y & 0x1) * 2 + ((x+z) & 0x1);
        return state.withProperty(ICON_TOPDOWN, topdown).withProperty(ICON_SIDE, side);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ICON_TOPDOWN, ICON_SIDE);
    }
}
