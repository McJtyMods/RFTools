package mcjty.rftools.blocks.builder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.IProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

public class SupportBlock extends Block {

    public static final int STATUS_OK = 0;
    public static final int STATUS_WARN = 1;
    public static final int STATUS_ERROR = 2;

    public static IntegerProperty STATUS = IntegerProperty.create("status", 0, 2);

    public SupportBlock() {
        super(Block.Properties.create(Material.GLASS)

        );
        // @todo 1.14
//        super(Material.GLASS, MapColor.CYAN);
        setRegistryName("support_block");
//        setCreativeTab(RFTools.setup.getTab());
    }

//    public static boolean activateBlock(Block block, World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
//        return block.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
//    }

    public static Collection<IProperty<?>> getPropertyKeys(BlockState state) {
        return state.getProperties();
    }

//    public void initModel() {
//        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
//    }

    // @todo 1.14
//    @Override
//    public boolean isOpaqueCube(BlockState state) {
//        return false;
//    }
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    public BlockRenderLayer getBlockLayer() {
//        return BlockRenderLayer.TRANSLUCENT;
//    }
//
//    @Override
//    public boolean isBlockNormalCube(BlockState state) {
//        return false;
//    }

    // @todo 1.14 loot tables
//    @Override
//    public Item getItemDropped(BlockState state, Random rand, int fortune) {
//        return null;
//    }

//    @Override
//    public int quantityDropped(Random random) {
//        return 0;
//    }
//
//    @Override
//    public boolean canSilkHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
//        return false;
//    }


    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!world.isRemote) {
            // Find all connected blocks and remove them.
            Deque<BlockPos> todo = new ArrayDeque<>();
            todo.add(pos);
            removeBlock(world, todo);
        }
        return super.onBlockActivated(state, world, pos, player, handIn, hit);
    }

    private void removeBlock(World world, Deque<BlockPos> todo) {
        while (!todo.isEmpty()) {
            BlockPos c = todo.pollFirst();
            world.setBlockState(c, Blocks.AIR.getDefaultState());
            if (world.getBlockState(c.west()).getBlock() == this) {
                todo.push(c.west());
            }
            if (world.getBlockState(c.east()).getBlock() == this) {
                todo.push(c.east());
            }
            if (world.getBlockState(c.down()).getBlock() == this) {
                todo.push(c.down());
            }
            if (world.getBlockState(c.up()).getBlock() == this) {
                todo.push(c.up());
            }
            if (world.getBlockState(c.south()).getBlock() == this) {
                todo.push(c.south());
            }
            if (world.getBlockState(c.north()).getBlock() == this) {
                todo.push(c.north());
            }
        }
    }

    // @todo 1.14
//    @SideOnly(Side.CLIENT)
//    @Override
//    public boolean shouldSideBeRendered(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
//        BlockState state = blockAccess.getBlockState(pos);
//        Block block = state.getBlock();
//        BlockState state2 = blockAccess.getBlockState(pos.offset(side));
//        Block block2 = state2.getBlock();
//        if (block.getMetaFromState(state) != block2.getMetaFromState(state2)) {
//            return true;
//        }
//
//        if (block2 == this) {
//            return false;
//        }
//
//        return block2 != this && super.shouldSideBeRendered(blockState, blockAccess, pos, side);
//    }


    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(STATUS);
    }


//    @Override
//    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> tab) {
//        super.getSubBlocks(itemIn, tab);
//    }

}
