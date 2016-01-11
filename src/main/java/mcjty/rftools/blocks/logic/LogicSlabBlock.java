package mcjty.rftools.blocks.logic;

import mcjty.lib.entity.GenericTileEntity;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.inventory.Container;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;

/**
 * The superclass for all logic slabs in RFTools.
 */
public abstract class LogicSlabBlock<T extends GenericTileEntity, C extends Container> extends GenericRFToolsBlock {

    public static PropertyBool OUTPUTPOWER = PropertyBool.create("output");


    public LogicSlabBlock(Material material, String name, Class<? extends T> tileEntityClass, Class<? extends C> containerClass) {
        super(material, tileEntityClass, containerClass, name, false);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.3F, 1.0F);
    }

    @Override
    public void initModel() {
        super.initModel();
        StateMap.Builder ignorePower = new StateMap.Builder().ignore(OUTPUTPOWER);
        ModelLoader.setCustomStateMapper(this, ignorePower.build());
    }

    /**
     * Returns the signal strength at one input of the block
     */
    private int getInputStrength(World world, BlockPos pos, EnumFacing side) {
        return world.getRedstonePower(pos.offset(side), side);
//        int dir = Direction.facingToDirection[side];
//        int xoffset = x + Direction.offsetX[dir];
//        int zoffset = z + Direction.offsetZ[dir];
//        int power = world.getIndirectPowerLevelTo(xoffset, y, zoffset, side);
//        int wirePower = world.getBlock(xoffset, y, zoffset) == Blocks.redstone_wire ? world.getBlockMetadata(xoffset, y, zoffset) : 0;
//        return power >= 15 ? power : Math.max(power, wirePower);
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        // @todo check!
        checkRedstoneWithTE(world, pos);
//        int meta = world.getBlockMetadata(x, y, z);
//        ForgeDirection k = BlockTools.getOrientationHoriz(meta);
//        int power = getInputStrength(world, x, y, z, k.ordinal());
//        meta = BlockTools.setRedstoneSignalIn(meta, power > 0);
//        world.setBlockMetadataWithNotify(x, y, z, meta, 2);
    }

    @Override
    public boolean isFullBlock() {
        return super.isFullBlock();
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
        return null;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing side) {
        IBlockState state = world.getBlockState(pos);
        EnumFacing direction = state.getValue(FACING);
        switch (direction) {
            case DOWN:
            case UP:
                return side == EnumFacing.DOWN || side == EnumFacing.UP;
            case NORTH:
            case SOUTH:
                return side == EnumFacing.NORTH || side == EnumFacing.SOUTH;
            case WEST:
            case EAST:
                return side == EnumFacing.WEST || side == EnumFacing.EAST;
        }
        return false;
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public int getWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
        EnumFacing direction = state.getValue(FACING);
        if (side == direction) {
            return state.getValue(OUTPUTPOWER) ? 15 : 0;
        } else {
            return 0;
        }
    }


    @Override
    public int getStrongPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
        return getWeakPower(world, pos, state, side);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, getFacing(meta & 7)).withProperty(OUTPUTPOWER, (meta&8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex() + (state.getValue(OUTPUTPOWER) ? 8 : 0);
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, FACING, OUTPUTPOWER);
    }


}
