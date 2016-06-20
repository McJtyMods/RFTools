package mcjty.rftools.blocks.logic.generic;

import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static net.minecraft.util.EnumFacing.*;

/**
 * The superclass for all logic slabs in RFTools.
 */
public abstract class LogicSlabBlock<T extends LogicTileEntity, C extends Container> extends GenericRFToolsBlock<T, C> {

    public static PropertyBool OUTPUTPOWER = PropertyBool.create("output");
    public static PropertyInteger META_INTERMEDIATE = PropertyInteger.create("intermediate", 0, 3);
    public static PropertyEnum<LogicFacing> LOGIC_FACING = PropertyEnum.create("logic_facing", LogicFacing.class);

    public LogicSlabBlock(Material material, String name, Class<? extends T> tileEntityClass, Class<? extends C> containerClass) {
        super(material, tileEntityClass, containerClass, LogicItemBlock.class, name, false);
    }

    @Override
    public boolean hasNoRotation() {
        return true;
    }

    @Override
    public boolean hasRedstoneOutput() {
        return true;
    }

    @Override
    public boolean needsRedstoneCheck() {
        return true;
    }

    public LogicSlabBlock(Material material, String name, Class<? extends T> tileEntityClass, Class<? extends C> containerClass, Class<? extends ItemBlock> itemBlockClass) {
        super(material, tileEntityClass, containerClass, itemBlockClass, name, false);
    }

    public static final AxisAlignedBB BLOCK_DOWN = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.3F, 1.0F);
    public static final AxisAlignedBB BLOCK_UP = new AxisAlignedBB(0.0F, 0.7F, 0.0F, 1.0F, 1.0F, 1.0F);
    public static final AxisAlignedBB BLOCK_NORTH = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.3F);
    public static final AxisAlignedBB BLOCK_SOUTH = new AxisAlignedBB(0.0F, 0.0F, 0.7F, 1.0F, 1.0F, 1.0F);
    public static final AxisAlignedBB BLOCK_WEST = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 0.3F, 1.0F, 1.0F);
    public static final AxisAlignedBB BLOCK_EAST = new AxisAlignedBB(0.7F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() instanceof LogicSlabBlock) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof LogicTileEntity) {
                LogicTileEntity logicTileEntity = (LogicTileEntity) te;
                EnumFacing side = logicTileEntity.getFacing().getSide();
                switch (side) {
                    case DOWN:
                        return BLOCK_DOWN;
                    case UP:
                        return BLOCK_UP;
                    case NORTH:
                        return BLOCK_NORTH;
                    case SOUTH:
                        return BLOCK_SOUTH;
                    case WEST:
                        return BLOCK_WEST;
                    case EAST:
                        return BLOCK_EAST;
                }
            }
        }
        return BLOCK_DOWN;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void initModel() {
        super.initModel();
        StateMap.Builder ignorePower = new StateMap.Builder().ignore(OUTPUTPOWER).ignore(META_INTERMEDIATE);
        ModelLoader.setCustomStateMapper(this, ignorePower.build());
    }

    /**
     * Returns the signal strength at one input of the block
     */
    protected int getInputStrength(World world, BlockPos pos, EnumFacing side) {
        return world.getRedstonePower(pos.offset(side), side);
    }

    @Override
    protected void checkRedstone(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof LogicTileEntity) {
            LogicTileEntity logicTileEntity = (LogicTileEntity)te;
            EnumFacing inputSide = logicTileEntity.getFacing().getInputSide();
            // @todo
            int power = getInputStrength(world, pos, inputSide);
            if (power == 0) {
                // Check if there is no redstone wire there. If there is a 'bend' in the redstone wire it is
                // not detected with getInputStrength().
                // @todo this is a bit of a hack. Don't know how to do it better right now
                IBlockState blockState = world.getBlockState(pos.offset(inputSide));
                Block b = blockState.getBlock();
                if (b == Blocks.REDSTONE_WIRE) {
                    power = world.isBlockPowered(pos.offset(inputSide)) ? 15 : 0;
                }
            }
            logicTileEntity.setPowerInput(power);
        }
    }

//    @Override
//    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
//        return null;
//    }

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


    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof LogicTileEntity) {
            LogicTileEntity logicTileEntity = (LogicTileEntity)te;
            EnumFacing direction = logicTileEntity.getFacing().getInputSide();
            switch (direction) {
                case NORTH:
                case SOUTH:
                    return side == NORTH || side == SOUTH;
                case WEST:
                case EAST:
                    return side == WEST || side == EAST;
                case DOWN:
                case UP:
                    return side == DOWN || side == UP;
            }
        }
        return false;
    }

    @Override
    protected int getRedstoneOutput(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof LogicTileEntity) {
            LogicTileEntity logicTileEntity = (LogicTileEntity) te;
            if (side == logicTileEntity.getFacing().getInputSide()) {
                return state.getValue(OUTPUTPOWER) ? 15 : 0;
            } else {
                return 0;
            }
        }
        return 0;
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof LogicTileEntity) {
            LogicTileEntity logicTileEntity = (LogicTileEntity) te;
            LogicFacing facing = logicTileEntity.getFacing();
            int meta = facing.getMeta();
            switch (meta) {
                case 0: meta = 2; break;
                case 1: meta = 3; break;
                case 2: meta = 1; break;
                case 3: meta = 0; break;
            }
            LogicFacing newfacing = LogicFacing.getFacingWithMeta(facing, meta);
            logicTileEntity.setFacing(newfacing);
            world.setBlockState(pos, world.getBlockState(pos).getBlock().getDefaultState()
                    .withProperty(META_INTERMEDIATE, meta)
                    .withProperty(OUTPUTPOWER, false), 3);
            return true;
        }
        return false;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        int meta = state.getValue(META_INTERMEDIATE);
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof LogicTileEntity) {
            LogicTileEntity logicTileEntity = (LogicTileEntity) te;
            LogicFacing facing = logicTileEntity.getFacing();
            facing = LogicFacing.getFacingWithMeta(facing, meta);
            return state.withProperty(LOGIC_FACING, facing);
        } else {
            return state.withProperty(LOGIC_FACING, LogicFacing.DOWN_TONORTH);
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(META_INTERMEDIATE, meta & 3).withProperty(OUTPUTPOWER, (meta & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(META_INTERMEDIATE) + (state.getValue(OUTPUTPOWER) ? 8 : 0);
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LOGIC_FACING, META_INTERMEDIATE, OUTPUTPOWER);
    }


}
