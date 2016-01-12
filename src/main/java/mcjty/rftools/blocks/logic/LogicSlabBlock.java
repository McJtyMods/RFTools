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
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
    public boolean isHorizRotation() {
        return true;
    }

    public LogicSlabBlock(Material material, String name, Class<? extends T> tileEntityClass, Class<? extends C> containerClass, Class<? extends ItemBlock> itemBlockClass) {
        super(material, tileEntityClass, containerClass, itemBlockClass, name, false);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.3F, 1.0F);
    }

    @SideOnly(Side.CLIENT)
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
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof GenericTileEntity) {
            int powered = getInputStrength(world, pos, state.getValue(FACING_HORIZ));
            GenericTileEntity genericTileEntity = (GenericTileEntity)te;
            genericTileEntity.setPowered(powered);
        }
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
        EnumFacing direction = state.getValue(FACING_HORIZ);
        switch (direction) {
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
        EnumFacing direction = state.getValue(FACING_HORIZ);
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
        return getDefaultState().withProperty(FACING_HORIZ, getFacingHoriz(meta & 3)).withProperty(OUTPUTPOWER, (meta & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(FACING_HORIZ).getIndex() - 2) + (state.getValue(OUTPUTPOWER) ? 8 : 0);
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, FACING_HORIZ, OUTPUTPOWER);
    }


}
